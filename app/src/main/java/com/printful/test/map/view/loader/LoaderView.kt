package com.printful.test.map.view.loader

import android.transition.TransitionManager
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.printful.test.R

class LoaderView(private val parent: ConstraintLayout) {

    private val loader = parent.findViewById<FrameLayout>(R.id.loader)

    data class State(val visible: Boolean)

    fun render(state: State) {
        TransitionManager.beginDelayedTransition(parent)
        loader.isVisible = state.visible
    }
}