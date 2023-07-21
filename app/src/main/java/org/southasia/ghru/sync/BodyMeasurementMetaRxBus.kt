package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.BloodPressureMetaRequest
import org.southasia.ghru.vo.request.BodyMeasurementMeta


class BodyMeasurementMetaRxBus private constructor() {

    private val relay: PublishRelay<BodyMeasurementMeta>

    init {
        relay = PublishRelay.create()
    }

    fun post(bodyMeasurementMeta: BodyMeasurementMeta) {
        relay.accept(bodyMeasurementMeta)
    }

    fun toObservable(): Observable<BodyMeasurementMeta> {
        return relay
    }

    companion object {

        private var instance: BodyMeasurementMetaRxBus? = null

        @Synchronized
        fun getInstance(): BodyMeasurementMetaRxBus {
            if (instance == null) {
                instance = BodyMeasurementMetaRxBus()
            }
            return instance as BodyMeasurementMetaRxBus
        }
    }
}