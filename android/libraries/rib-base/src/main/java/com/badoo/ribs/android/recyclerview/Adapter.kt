package com.badoo.ribs.android.recyclerview

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.badoo.ribs.android.recyclerview.RecyclerViewHost.HostingStrategy.EAGER
import com.badoo.ribs.android.recyclerview.RecyclerViewHost.HostingStrategy.LAZY
import com.badoo.ribs.android.recyclerview.RecyclerViewHost.Input
import com.badoo.ribs.android.recyclerview.RecyclerViewHostFeature.State.Entry
import com.badoo.ribs.core.Node
import com.badoo.ribs.core.routing.RoutingSource
import com.badoo.ribs.core.routing.activator.ChildActivator
import com.badoo.ribs.core.routing.history.Routing
import com.badoo.ribs.util.RIBs
import io.reactivex.functions.Consumer
import java.lang.ref.WeakReference

internal class Adapter<T : Parcelable>(
    private val hostingStrategy: RecyclerViewHost.HostingStrategy,
    initialEntries: List<Entry<T>>? = null,
    private val routingSource: RoutingSource.Pool<T>,
    private val feature: RecyclerViewHostFeature<T>,
    private val viewHolderLayoutParams: FrameLayout.LayoutParams
) : RecyclerView.Adapter<Adapter.ViewHolder>(),
    Consumer<RecyclerViewHostFeature.State<T>>,
    ChildActivator<T> {

    private val holders: MutableMap<Routing.Identifier, WeakReference<ViewHolder>> = mutableMapOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var identifier: Routing.Identifier? = null
    }

    private var items: List<Entry<T>> = initialEntries ?: emptyList()

    override fun getItemCount(): Int =
        items.size

    override fun accept(state: RecyclerViewHostFeature.State<T>) {
        items = state.items

        when (state.lastCommand) {
            null -> { /* No-op when restored from TimeCapsule or genuinely empty state */ }
            is Input.Add -> {
                eagerAdd(state.items.last())
                notifyItemInserted(state.items.lastIndex)
            }
        }
    }

    private fun eagerAdd(entry: Entry<T>) {
        if (hostingStrategy == EAGER) {
            routingSource.add(entry.element, entry.identifier)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            FrameLayout(parent.context).apply {
                layoutParams = viewHolderLayoutParams
            }
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = items[position]
        holder.identifier = entry.identifier
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val identifier = holder.identifier!! // at this point it should be bound
        holders[identifier] = WeakReference(holder)

        if (hostingStrategy == LAZY) {
            val entry = feature.state.items.find { it.identifier == identifier }!!
            routingSource.add(entry.element, entry.identifier)
        }

        routingSource.activate(identifier)
    }

    override fun activate(routing: Routing<T>, child: Node<*>) {
        holders[routing.identifier]?.get()?.let { holder ->
            child.attachToView(holder.itemView as FrameLayout)
        } ?: RIBs.errorHandler.handleNonFatalError("Holder is gone! Routing: $routing, child: $child")
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        val identifier = holder.identifier!! // at this point it should be bound

        deactivate(identifier)
        if (hostingStrategy == LAZY) {
            routingSource.remove(identifier)
        }
    }

    internal fun onDestroy() {
        items.forEach {
            deactivate(it.identifier)
        }
    }

    private fun deactivate(identifier: Routing.Identifier) {
        routingSource.deactivate(identifier)
    }

    override fun deactivate(routing: Routing<T>, child: Node<*>) {
        child.detachFromView()
    }
}
