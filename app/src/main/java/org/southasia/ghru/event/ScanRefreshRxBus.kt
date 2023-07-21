package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class ScanRefreshRxBus private constructor() {
    private val relay: PublishRelay<Boolean>

    init {
        relay = PublishRelay.create()
    }

    fun post(barcode: Boolean) {
        relay.accept(barcode)
    }

    fun toObservable(): Observable<Boolean> {
        return relay
    }

    companion object {

        private var instance: ScanRefreshRxBus? = null

        @Synchronized
        fun getInstance(): ScanRefreshRxBus {
            if (instance == null) {
                instance = ScanRefreshRxBus()
            }
            return instance as ScanRefreshRxBus
        }
    }
}