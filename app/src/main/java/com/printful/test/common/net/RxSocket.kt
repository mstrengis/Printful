package com.printful.test.common.net

import io.reactivex.Completable
import io.reactivex.Flowable

interface RxSocket {

    sealed class State {
        object Idle : State()
        object Connecting : State()
        object Connected : State()
    }

    fun connect(): Flowable<State>
    fun disconnect(): Completable
    fun read(): Flowable<String>
    fun write(message: String): Completable
}