package com.badoo.ribs.template.leaf.foo_bar

import com.badoo.ribs.core.Rib
import com.badoo.ribs.customisation.CanProvideRibCustomisation
import com.badoo.ribs.customisation.RibCustomisation
import io.reactivex.ObservableSource
import io.reactivex.Single
import io.reactivex.functions.Consumer

interface FooBar : Rib {

    interface Dependency : CanProvideRibCustomisation {
        fun fooBarInput(): ObservableSource<Input>
        fun fooBarOutput(): Consumer<Output>
    }

    sealed class Input

    sealed class Output

    class Customisation(
        val viewFactory: FooBarView.Factory = FooBarViewImpl.Factory()
    ) : RibCustomisation

    interface Workflow {
        // todo: do not delete - rename, and add more
        // todo: expose all meaningful operations
        fun businessLogicOperation(): Single<FooBar.Workflow>

        // todo: expose all possible children (even permanent parts), or remove if there's none
        // fun attachChild1(): Single<Child.Workflow>
    }
}