package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable


class QRcodeRxBus private constructor() {
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

        private var instance: QRcodeRxBus? = null

        @Synchronized
        fun getInstance(): QRcodeRxBus {
            if (instance == null) {
                instance = QRcodeRxBus()
            }
            return instance as QRcodeRxBus
        }
    }
}