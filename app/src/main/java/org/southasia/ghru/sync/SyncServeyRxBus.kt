package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class SyncServeyRxBus private constructor() {
    private val relay: PublishRelay<SyncServeyResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, json: String) {
        relay.accept(SyncServeyResponse(eventType, json))
    }

    fun toObservable(): Observable<SyncServeyResponse> {
        return relay
    }

    companion object {

        private var instance: SyncServeyRxBus? = null

        @Synchronized
        fun getInstance(): SyncServeyRxBus {
            if (instance == null) {
                instance = SyncServeyRxBus()
            }
            return instance as SyncServeyRxBus
        }
    }
}