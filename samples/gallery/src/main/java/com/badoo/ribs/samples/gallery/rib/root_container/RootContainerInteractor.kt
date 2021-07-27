package com.badoo.ribs.samples.gallery.rib.root_container

import androidx.lifecycle.Lifecycle
import com.badoo.ribs.clienthelper.childaware.childAware
import com.badoo.mvicore.android.lifecycle.createDestroy
import com.badoo.ribs.clienthelper.interactor.Interactor
import com.badoo.ribs.core.modality.BuildParams
import com.badoo.ribs.routing.source.backstack.BackStack
import com.badoo.ribs.routing.source.backstack.operation.push
import com.badoo.ribs.samples.gallery.rib.root_picker.RootPicker
import com.badoo.ribs.samples.gallery.rib.root_container.routing.RootContainerRouter.Configuration
import io.reactivex.functions.Consumer

internal class RootContainerInteractor(
    buildParams: BuildParams<*>,
    private val backStack: BackStack<Configuration>,
) : Interactor<RootContainer, RootContainerView>(
    buildParams = buildParams
) {

    override fun onCreate(nodeLifecycle: Lifecycle) {
        childAware(nodeLifecycle) {
            whenChildAttached<RootPicker> { commonLifecycle, picker ->
                commonLifecycle.createDestroy {
                    bind(picker.output to Consumer { onPickerOutput(it) })
                }
            }
        }
    }

    private fun onPickerOutput(output: RootPicker.Output) {
        val configuration: Configuration = when (output) {
            RootPicker.Output.Sample1Picked -> TODO()
        }

        backStack.push(configuration)
    }
}
