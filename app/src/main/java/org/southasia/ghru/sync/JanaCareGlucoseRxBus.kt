package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class JanaCareGlucoseRxBus private constructor() {
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

        private var instance: JanaCareGlucoseRxBus? = null

        @Synchronized
        fun getInstance(): JanaCareGlucoseRxBus {
            if (instance == null) {
                instance = JanaCareGlucoseRxBus()
            }
            return instance as JanaCareGlucoseRxBus
        }
    }
}