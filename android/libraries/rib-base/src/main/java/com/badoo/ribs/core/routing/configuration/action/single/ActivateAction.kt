package com.badoo.ribs.core.routing.configuration.action.single

import android.os.Parcelable
import com.badoo.ribs.core.Node
import com.badoo.ribs.core.routing.action.RoutingAction
import com.badoo.ribs.core.routing.activator.RoutingActivator
import com.badoo.ribs.core.routing.configuration.ConfigurationContext
import com.badoo.ribs.core.routing.configuration.ConfigurationContext.ActivationState.ACTIVE
import com.badoo.ribs.core.routing.configuration.ConfigurationContext.Resolved
import com.badoo.ribs.core.routing.configuration.action.ActionExecutionParams
import com.badoo.ribs.core.routing.configuration.feature.ConfigurationFeature.Effect
import com.badoo.ribs.core.routing.configuration.feature.EffectEmitter
import com.badoo.ribs.core.routing.history.Routing
import com.badoo.ribs.core.routing.transition.TransitionDirection
import com.badoo.ribs.core.routing.transition.TransitionElement

/**
 * Attaches views of associated [Node]s to a parentNode, and executes the associated [RoutingAction].
 *
 * The [Node]s are expected to be already added to the parentNode on a logical level.
 */
internal class ActivateAction<C : Parcelable>(
    private val emitter: EffectEmitter<C>,
    private val routing: Routing<C>,
    private var item: Resolved<C>,
    private val parentNode: Node<*>,
    private val activator: RoutingActivator<C>,
    private val isBackStackOperation: Boolean,
    private val globalActivationLevel: ConfigurationContext.ActivationState
) : Action<C> {

    object Factory: ActionFactory {
        override fun <C : Parcelable> create(
            params: ActionExecutionParams<C>
        ): Action<C> =
            ActivateAction(
                emitter = params.transactionExecutionParams.emitter,
                routing = params.routing,
                item = params.item,
                parentNode = params.transactionExecutionParams.parentNode,
                activator = params.transactionExecutionParams.activator,
                isBackStackOperation = params.isBackStackOperation,
                globalActivationLevel = params.transactionExecutionParams.globalActivationLevel
            )
    }

    override var canExecute: Boolean =
        false

    override var transitionElements: List<TransitionElement<C>> =
        emptyList()

    override fun onBeforeTransition() {
        val environmentActivated = globalActivationLevel == ACTIVE
        val itemAlreadyActivated = item.activationState == ACTIVE
        canExecute = environmentActivated && !itemAlreadyActivated

        // The least we can do is to mark correct state, this is regardless of executing transitions
        if (!itemAlreadyActivated) {
            emitter.onNext(
                Effect.Individual.Activated(routing, item.copy(activationState = globalActivationLevel))
            )
        }

        if (canExecute) {
            activator.activate(routing, item.nodes)
            prepareTransition()
        }
    }

    /**
     * TODO Consider doing this in Router when callback is fired. This would fix
     *  actionableNodes vs item.nodes disparity here and make the decision more pronounced.
     */
    private fun prepareTransition() {
        transitionElements = item.nodes.mapNotNull {
            it.view?.let { ribView ->
                TransitionElement(
                    configuration = item.routing.configuration, // TODO consider passing the whole RoutingElement
                    direction = TransitionDirection.ENTER,
                    isBackStackOperation = isBackStackOperation,
                    parentViewGroup = parentNode.targetViewGroupForChild(it),
                    identifier = it.identifier,
                    view = ribView.androidView
                )
            }
        }
    }

    override fun onTransition(forceExecute: Boolean) {
        if (canExecute || forceExecute) {
            item.routingAction.execute()

            emitter.onNext(
                Effect.Individual.PendingDeactivateFalse(routing)
            )
        }
    }

    override fun onFinish(forceExecute: Boolean) {
    }
}
