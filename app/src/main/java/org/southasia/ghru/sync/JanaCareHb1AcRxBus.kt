package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class JanaCareHb1AcRxBus private constructor() {
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

        private var instance: JanaCareHb1AcRxBus? = null

        @Synchronized
        fun getInstance(): JanaCareHb1AcRxBus {
            if (instance == null) {
                instance = JanaCareHb1AcRxBus()
            }
            return instance as JanaCareHb1AcRxBus
        }
    }
}