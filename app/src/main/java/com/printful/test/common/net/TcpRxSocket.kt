package com.printful.test.common.net

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.net.Socket
import java.nio.charset.Charset

class TcpRxSocket(private val host: String, private val port: Int) : RxSocket {

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null

    private var socketEmitter: FlowableEmitter<RxSocket.State>? = null
    private var readEmitter: FlowableEmitter<String>? = null

    override fun connect(): Flowable<RxSocket.State> {
        return Flowable
            .create<RxSocket.State>(
                { emitter ->
                    socketEmitter = emitter
                    emitter.onNext(RxSocket.State.Connecting)
                    socket = Socket(host, port).also {
                        writer = BufferedWriter(
                            OutputStreamWriter(
                                it.getOutputStream(),
                                Charset.forName("UTF-8")
                            )
                        )
                        reader = BufferedReader(InputStreamReader(it.getInputStream()))
                    }
                    emitter.onNext(RxSocket.State.Connected)
                },
                BackpressureStrategy.LATEST
            )
            .startWith(RxSocket.State.Idle)
            .subscribeOn(Schedulers.io())
    }

    override fun disconnect(): Completable {
        return Completable.fromAction {
            socket?.closeSilently()
            writer?.closeSilently()
            reader?.closeSilently()

            readEmitter?.onComplete()
            readEmitter = null

            socketEmitter?.onNext(RxSocket.State.Idle)
            socketEmitter?.onComplete()
            socketEmitter = null
        }
    }

    fun Closeable.closeSilently() {
        try {
            close()
        } catch (error: IOException) {

        }
    }

    override fun read(): Flowable<String> {
        return Flowable
            .create<String>(
                { emitter ->
                    readEmitter = emitter
                    while (readEmitter?.isCancelled == false) {
                        val reader = reader
                        if (reader == null) {
                            val error = IllegalStateException("No reader")
                            emitter.tryOnError(error)
                            socketEmitter?.tryOnError(error)
                        } else {
                            try {
                                val line = reader.readLine()
                                if (line == null) {
                                    val error = IllegalStateException("SocketClosed")
                                    emitter.tryOnError(error)
                                    socketEmitter?.tryOnError(error)
                                } else {
                                    emitter.onNext(line)
                                }
                            } catch (error: IOException) {
                                socketEmitter?.tryOnError(error)
                            }
                        }
                    }
                },
                BackpressureStrategy.BUFFER
            )
            .doOnError { socketEmitter?.tryOnError(it) }
            .subscribeOn(Schedulers.io())
    }

    override fun write(message: String): Completable {
        val writer = writer
        if (writer == null) {
            val error = IllegalStateException("No writer")
            socketEmitter?.tryOnError(error)
            return Completable.error(error)
        }

        return Completable
            .fromAction {
                writer.write(message)
                writer.newLine()
                writer.flush()
            }
            .doOnError { socketEmitter?.tryOnError(it) }
            .subscribeOn(Schedulers.io())
    }
}