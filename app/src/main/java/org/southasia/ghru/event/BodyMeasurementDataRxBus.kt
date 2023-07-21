package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.BodyMeasurementData

class BodyMeasurementDataRxBus private constructor() {
    private val relay: PublishRelay<BodyMeasurementDataResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(bodyMeasurementDataResponse: BodyMeasurementDataResponse) {
        relay.accept(bodyMeasurementDataResponse)
    }

    fun toObservable(): Observable<BodyMeasurementDataResponse> {
        return relay
    }

    companion object {

        private var instance: BodyMeasurementDataRxBus? = null

        @Synchronized
        fun getInstance(): BodyMeasurementDataRxBus {
            if (instance == null) {
                instance = BodyMeasurementDataRxBus()
            }
            return instance as BodyMeasurementDataRxBus
        }
    }
}

class BodyMeasurementDataResponse(
    val eventType: BodyMeasurementDataEventType,
    val bodyMeasurementData: BodyMeasurementData
)

enum class BodyMeasurementDataEventType {
    HEIGHT,
    BODY_COMOSITION,
    HIP_WAIST
}
