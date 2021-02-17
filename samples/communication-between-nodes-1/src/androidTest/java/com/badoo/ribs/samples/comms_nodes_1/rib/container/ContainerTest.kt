package com.badoo.ribs.samples.comms_nodes_1.rib.container

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.badoo.common.ribs.RibsRule
import com.badoo.ribs.core.modality.BuildContext
import com.badoo.ribs.samples.comms_nodes_1.R
import org.junit.Rule
import org.junit.Test

class ContainerTest {

    @get:Rule
    val ribsRule = RibsRule { _, savedInstanceState -> buildRib(savedInstanceState) }

    private fun buildRib(savedInstanceState: Bundle?) =
        ContainerBuilder(object : Container.Dependency {})
            .build(BuildContext.root(savedInstanceState))

    @Test
    fun default() {
        then { iSeeChild1() }
    }

    @Test
    fun navigateToChild2() {
        whenever { iNavigateToChild2() }
        then { iSeeChild2() }
    }

    @Test
    fun navigateToChild3() {
        whenever { iNavigateToChild3() }
        then { iSeeChild3() }
    }

    @Test
    fun multipleNavigation() {
        given { iNavigateToChild2() }
        whenever { iNavigateToChild1() }
        then { iSeeChild1() }
    }

    @Test
    fun backStack() {
        given { iNavigateToChild2() }
        and { iNavigateToChild3() }
        then { iSeeChild3() }

        whenever { iPressBack() }
        then { iSeeChild2() }

        whenever { iPressBack() }
        then { iSeeChild1() }
    }

    // region Helper functions

    private fun iNavigateToChild1() {
        iClick(R.id.child1)
    }

    private fun iNavigateToChild2() {
        iClick(R.id.child2)
    }

    private fun iNavigateToChild3() {
        iClick(R.id.child3)
    }

    private fun iSeeChild1() {
        iSeeViewWithText("Hello! I am Child 1")
    }

    private fun iSeeChild2() {
        iSeeViewWithText("Hello! I am Child 2")
    }

    private fun iSeeChild3() {
        iSeeViewWithText("Hello! I am Child 3")
    }

    private fun iClick(@IdRes idRes: Int) {
        onView(withId(idRes)).perform(click())
    }

    private fun iSeeViewWithText(text: String) {
        onView(withText(text)).check(matches(isDisplayed()))
    }

    private fun iPressBack() {
        pressBack()
    }

    private fun given(step: () -> Unit) { step() }
    private fun whenever(step: () -> Unit) { step() }
    private fun then(step: () -> Unit) { step() }
    private fun and(step: () -> Unit) { step() }

    // endregion
}
