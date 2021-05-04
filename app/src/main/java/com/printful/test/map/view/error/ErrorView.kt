package com.printful.test.map.view.error

import android.transition.TransitionManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.printful.test.R
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class ErrorView(private val root: ConstraintLayout) {

    private val error = root.findViewById<LinearLayout>(R.id.error)
    private val retry = root.findViewById<Button>(R.id.retry)

    private val events = PublishSubject.create<Event>()

    data class State(val visible: Boolean)

    sealed class Event {
        object RetryClicked : Event()
    }

    init {
        retry.setOnClickListener { events.onNext(Event.RetryClicked) }
    }

    fun events(): Flowable<Event> = events.toFlowable(BackpressureStrategy.BUFFER)

    fun render(state: State) {
        TransitionManager.beginDelayedTransition(root)
        error.isVisible = state.visible
    }
}