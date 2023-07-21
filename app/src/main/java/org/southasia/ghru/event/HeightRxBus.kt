package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.BodyMeasurementData

class HeightRxBus private constructor() {
    private val relay: PublishRelay<BodyMeasurementData>

    init {
        relay = PublishRelay.create()
    }

    fun post(bodyMeasurementData: BodyMeasurementData) {
        relay.accept(bodyMeasurementData)
    }

    fun toObservable(): Observable<BodyMeasurementData> {
        return relay
    }

    companion object {

        private var instance: HeightRxBus? = null

        @Synchronized
        fun getInstance(): HeightRxBus {
            if (instance == null) {
                instance = HeightRxBus()
            }
            return instance as HeightRxBus
        }
    }
}