package com.printful.test.map

import android.annotation.SuppressLint
import com.google.android.gms.maps.model.LatLng
import com.printful.test.common.net.RxSocket
import com.printful.test.map.geocoder.AddressLookup
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit


class UsersController(
    private val client: RxSocket,
    private val addressLookup: AddressLookup,
    private val ui: Scheduler = AndroidSchedulers.mainThread(),
    private val computation: Scheduler = Schedulers.computation()
) {

    companion object {
        internal const val DISCONNECT_DELAY_SECONDS = 1L
    }

    sealed class Input {
        object StartMonitoring : Input()
        object StopMonitoring : Input()
        object Reconnect : Input()
    }

    data class State(
        val users: List<User> = emptyList(),
        val socketState: RxSocket.State = RxSocket.State.Idle,
        val socketError: Throwable? = null,
    ) {
        data class User(
            val id: Int,
            val name: String,
            val image: String,
            val position: LatLng,
            val address: String? = null
        )
    }

    private var delayedConnectionKillDisposable = Disposables.disposed()
    private val monitoringSubs = CompositeDisposable()
    private val subs = CompositeDisposable()

    private var state = State()
    private val states: BehaviorSubject<State> = BehaviorSubject.createDefault(state)

    fun states(): Flowable<State> = states
        .toFlowable(BackpressureStrategy.LATEST)
        .observeOn(ui)

    fun input(input: Input) {
        when (input) {
            Input.StartMonitoring -> startMonitoring()
            Input.StopMonitoring -> stopMonitoring()
            Input.Reconnect -> reconnect()
        }.let { }
    }

    private fun startMonitoring() {
        if (delayedConnectionKillDisposable.isDisposed) {
            setState { copy(socketError = null) }

            client.connect()
                .subscribe(
                    { state ->
                        setState { copy(socketState = state, socketError = null) }
                        if (state == RxSocket.State.Connected) {
                            onSocketConnected()
                        }
                    },
                    { error ->
                        Timber.w(error, "Error connecting!")
                        setState { copy(socketState = RxSocket.State.Idle, socketError = error) }
                    }
                )
                .also(monitoringSubs::add)
        } else {
            delayedConnectionKillDisposable.dispose()
        }
    }

    private fun onSocketConnected() {
        client.write("AUTHORIZE martins.strengis@gmail.com")
            .toSingleDefault(Unit)
            .flatMapPublisher { client.read() }
            .map(MessageParser::parseMessage)
            .subscribe(
                { result ->
                    when (result) {
                        MessageParser.Result.InvalidMessage -> {
                            Timber.w("Invalid message!")
                        }
                        is MessageParser.Result.Update -> updateUser(result.id, result.latLng)
                        is MessageParser.Result.UserList -> {
                            setState { copy(users = result.users) }
                            result.users.forEach(::updateUserAddress)
                        }
                    }.let { }
                },
                {
                    Timber.w(it, "Error reading or authorizing")
                }
            )
            .also(monitoringSubs::add)
    }

    private fun updateUser(id: Int, position: LatLng) {
        val userToUpdate = state.users.firstOrNull { it.id == id }
        val user = userToUpdate?.copy(position = position)

        if (user != null) {
            updateUserList(user)
            updateUserAddress(user)
        }
    }

    private fun updateUserAddress(user: State.User) {
        addressLookup.getAddress(user.position.latitude, user.position.longitude)
            .subscribe(
                { addressResult ->
                    val update = when (addressResult) {
                        is AddressLookup.Result.Address -> user.copy(address = addressResult.address)
                        AddressLookup.Result.Error -> user.copy(address = null)
                    }
                    updateUserList(update)
                },
                { Timber.d(it, "Error getting address") }
            )
            .also(subs::add)
    }

    private fun updateUserList(user: State.User) {
        val userToUpdate = state.users.firstOrNull { it.id == user.id }
        val newList = state.users
            .toMutableList()
            .apply {
                remove(userToUpdate)
                add(user)
            }
            .toList()
            .sortedBy { it.id }

        setState { copy(users = newList) }
    }

    private fun stopMonitoring() {
        delayedConnectionKillDisposable =
            Single.timer(DISCONNECT_DELAY_SECONDS, TimeUnit.SECONDS, computation)
                .flatMapCompletable { client.disconnect() }
                .subscribe(
                    {
                        monitoringSubs.clear()
                        Timber.d("Disconnected!")
                    },
                    { Timber.w(it, "Error while disconnecting!") }
                )
                .also(subs::add)
    }


    private fun reconnect() {
        client.disconnect()
            .subscribe(
                {
                    monitoringSubs.clear()
                    Timber.d("Disconnected")
                    startMonitoring()
                },
                Timber::w
            )
            .also(subs::add)
    }

    private fun setState(update: State.() -> State) {
        state = state.update()
        states.onNext(state)
    }

    @SuppressLint("CheckResult")
    fun clear() {
        monitoringSubs.dispose()
        subs.dispose()

        client.disconnect()
            .subscribe(
                { Timber.d("Disconnected") },
                Timber::w
            )
    }
}