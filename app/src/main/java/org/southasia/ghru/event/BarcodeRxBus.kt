package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable


class BarcodeRxBus private constructor() {
    private val relay: PublishRelay<String>

    init {
        relay = PublishRelay.create()
    }

    fun post(barcode: String) {
        relay.accept(barcode)
    }

    fun toObservable(): Observable<String> {
        return relay
    }

    companion object {

        private var instance: BarcodeRxBus? = null

        @Synchronized
        fun getInstance(): BarcodeRxBus {
            if (instance == null) {
                instance = BarcodeRxBus()
            }
            return instance as BarcodeRxBus
        }
    }
}