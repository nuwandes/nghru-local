package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.BodyMeasurementRequest

class BodyMeasurementRequestRxBus private constructor() {
    private val relay: PublishRelay<BodyMeasurementRequestResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, bodyMeasurementRequest: BodyMeasurementRequest) {
        relay.accept(BodyMeasurementRequestResponse(eventType, bodyMeasurementRequest))
    }

    fun toObservable(): Observable<BodyMeasurementRequestResponse> {
        return relay
    }

    companion object {

        private var instance: BodyMeasurementRequestRxBus? = null

        @Synchronized
        fun getInstance(): BodyMeasurementRequestRxBus {
            if (instance == null) {
                instance = BodyMeasurementRequestRxBus()
            }
            return instance as BodyMeasurementRequestRxBus
        }
    }
}