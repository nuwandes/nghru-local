package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class StationCheckRxBus private constructor() {
    private val relay: PublishRelay<String>

    init {
        relay = PublishRelay.create()
    }

    fun post(s: String) {
        relay.accept(s)
    }

    fun toObservable(): Observable<String> {
        return relay
    }

    companion object {

        private var instance: StationCheckRxBus? = null

        @Synchronized
        fun getInstance(): StationCheckRxBus {
            if (instance == null) {
                instance = StationCheckRxBus()
            }
            return instance as StationCheckRxBus
        }
    }
}