package com.printful.test.map.view.loader

import com.printful.test.common.net.RxSocket
import com.printful.test.common.net.TcpRxSocket
import com.printful.test.map.UsersController
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher

class UsersControllerStateToViewState :
    FlowableTransformer<UsersController.State, LoaderView.State> {
    override fun apply(upstream: Flowable<UsersController.State>): Publisher<LoaderView.State> =
        upstream
            .map { LoaderView.State(it.socketState == RxSocket.State.Connecting) }
            .distinctUntilChanged()
}