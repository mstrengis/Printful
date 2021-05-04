package com.printful.test.map

import com.printful.test.common.net.RxSocket
import com.printful.test.map.geocoder.AddressLookup
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit


class UsersControllerTest {

    private lateinit var rxSocket: TestRxSocket
    private lateinit var addressLookup: TestAddressLookup
    private lateinit var controller: UsersController

    private val computation = TestScheduler()
    private val ui = Schedulers.trampoline()

    @Before
    fun setUp() {
        rxSocket = TestRxSocket()
        addressLookup = TestAddressLookup()
        controller = UsersController(rxSocket, addressLookup, ui, computation)
    }

    @Test
    fun `controller state is idle by default`() {
        val test = controller.states().test()
        test.assertValue {
            it.socketError == null && it.socketState == RxSocket.State.Idle && it.users.isEmpty()
        }
        test.dispose()
    }

    @Test
    fun `connection errors set socket state to idle and sets error`() {
        val error = OutOfMemoryError()
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onError(error)
        val test = controller.states().test()
        test.assertValue {
            it.socketError == error && it.socketState == RxSocket.State.Idle
        }
        test.dispose()
    }

    @Test
    fun `connection states is set in controller state`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connecting)
        val test = controller.states().test()
        test.assertValue { it.socketState == RxSocket.State.Connecting }
    }

    @Test
    fun `controller authorizes client when client is connected`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connecting)

        assert(!rxSocket.authorized)

        rxSocket.connectionState.onNext(RxSocket.State.Connected)

        val test = controller.states().test()
        test.assertValue { it.socketState == RxSocket.State.Connected }
        test.dispose()
    }

    @Test
    fun `controller saves users in state after client is connected and authorised`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connected)
        val test = controller.states().test()
        test.assertValue {
            it.users.size == 2 && it.users[0].id == rxSocket.testUserId && it.users[0].name == rxSocket.testUsername
        }
        test.dispose()
    }

    @Test
    fun `controller queries for locations for all initial users`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connected)
        val test = controller.states().test()
        test.assertValue {
            it.users[0].address == "${TestAddressLookup.ADDRESS_PREFIX}${it.users[0].position.latitude}" &&
                    it.users[1].address == "${TestAddressLookup.ADDRESS_PREFIX}${it.users[1].position.latitude}"
        }
        test.dispose()
    }

    @Test
    fun `controller queries for location when update received`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connected)
        rxSocket.messages.onNext("UPDATE 2,4.0,5.0")
        val test = controller.states().test()
        test.assertValue { state ->
            state.users.first { it.id == 2 }.address == "${TestAddressLookup.ADDRESS_PREFIX}4.0"
        }
        test.dispose()
    }

    @Test
    fun `controller disconnects only after a delay when stop monitoring received`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connected)
        rxSocket.messages.onNext("UPDATE 2,4.0,5.0")

        controller.input(UsersController.Input.StopMonitoring)
        val test = controller.states().test()
        test.assertValueAt(0) { it.socketState == RxSocket.State.Connected }
        computation.advanceTimeBy(UsersController.DISCONNECT_DELAY_SECONDS, TimeUnit.SECONDS)
        test.assertValueAt(1) { it.socketState == RxSocket.State.Idle }
        test.dispose()
    }

    @Test
    fun `controller doesn't disconnect if startMonitoring received before disconnect delay seconds has passed`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connected)
        rxSocket.messages.onNext("UPDATE 2,4.0,5.0")

        controller.input(UsersController.Input.StopMonitoring)
        val test = controller.states().test()
        test.assertValueAt(0) { it.socketState == RxSocket.State.Connected }

        controller.input(UsersController.Input.StartMonitoring)
        computation.advanceTimeBy(UsersController.DISCONNECT_DELAY_SECONDS, TimeUnit.SECONDS)
        test.assertValueCount(1)
        test.dispose()
    }

    @Test
    fun `reconnect disconnects and reconnects to client`() {
        controller.input(UsersController.Input.StartMonitoring)
        rxSocket.connectionState.onNext(RxSocket.State.Connected)
        controller.input(UsersController.Input.Reconnect)
        val test = controller.states().test()
        test.assertValue {
            it.socketState == RxSocket.State.Idle
        }

        assert(!rxSocket.authorized)
        rxSocket.connectionState.onNext(RxSocket.State.Connected)
        assert(rxSocket.authorized)

        test.dispose()
    }

    private class TestRxSocket : RxSocket {

        val testUserId = 1
        val testUsername = "Test User"

        val testUserId2 = 2
        val testUsername2 = "Test User2"

        var authorized = false
            private set

        val messages = BehaviorSubject.create<String>()
        var connectionState = PublishSubject.create<RxSocket.State>()
            private set

        override fun connect(): Flowable<RxSocket.State> {
            return connectionState.toFlowable(BackpressureStrategy.BUFFER)
        }

        override fun disconnect(): Completable {
            connectionState.onError(IllegalStateException("Disconnected"))
            connectionState = PublishSubject.create()
            authorized = false

            return Completable.complete()
        }

        override fun read(): Flowable<String> {
            return messages.toFlowable(BackpressureStrategy.BUFFER)
        }

        override fun write(message: String): Completable {
            messages.onNext("USERLIST $testUserId,$testUsername,image,1.0,1.0;$testUserId2,$testUsername2,image,2.0,2.0")
            return Completable.fromAction {
                authorized = true
            }
        }
    }

    private class TestAddressLookup : AddressLookup {

        companion object {
            const val ADDRESS_PREFIX = "Krasta iela "
        }

        override fun getAddress(latitude: Double, longitude: Double): Single<AddressLookup.Result> {
            return Single.just(
                when (latitude) {
                    0.0 -> AddressLookup.Result.Error
                    else -> AddressLookup.Result.Address("$ADDRESS_PREFIX$latitude")
                }
            )
        }
    }
}