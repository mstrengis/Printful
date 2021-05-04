package com.printful.test.map.view.error

import com.printful.test.map.UsersController
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher

class UsersControllerToErrorViewState :
    FlowableTransformer<UsersController.State, ErrorView.State> {
    override fun apply(upstream: Flowable<UsersController.State>): Publisher<ErrorView.State> =
        upstream
            .map { ErrorView.State(it.socketError != null) }
            .distinctUntilChanged()
}