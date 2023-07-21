package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class JanaCareCholesterolcomRxBus private constructor() {
    private val relay: PublishRelay<JanacareResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(number: JanacareResponse) {
        relay.accept(number)
    }

    fun toObservable(): Observable<JanacareResponse> {
        return relay
    }

    companion object {

        private var instance: JanaCareCholesterolcomRxBus? = null

        @Synchronized
        fun getInstance(): JanaCareCholesterolcomRxBus {
            if (instance == null) {
                instance = JanaCareCholesterolcomRxBus()
            }
            return instance as JanaCareCholesterolcomRxBus
        }
    }
}