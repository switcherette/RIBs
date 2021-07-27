package com.badoo.ribs.samples.gallery.rib.root_container.routing

import com.badoo.ribs.samples.gallery.rib.root_picker.RootPicker
import com.badoo.ribs.samples.gallery.rib.root_picker.RootPickerBuilder
import com.badoo.ribs.samples.gallery.rib.root_container.RootContainer

internal class RootContainerChildBuilders(
    dependency: RootContainer.Dependency
) {
    private val subtreeDeps = SubtreeDependency(dependency)

     val picker = RootPickerBuilder(subtreeDeps)

    class SubtreeDependency(
        dependency: RootContainer.Dependency
    ) : RootContainer.Dependency by dependency,
        RootPicker.Dependency
    {
    }
}



