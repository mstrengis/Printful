package com.printful.test.map.view.error

import com.printful.test.map.UsersController
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher

class ErrorEventsToUsersControllerInput :
    FlowableTransformer<ErrorView.Event, UsersController.Input> {
    override fun apply(upstream: Flowable<ErrorView.Event>): Publisher<UsersController.Input> =
        upstream
            .map { event ->
                when (event) {
                    ErrorView.Event.RetryClicked -> UsersController.Input.Reconnect
                }
            }
}