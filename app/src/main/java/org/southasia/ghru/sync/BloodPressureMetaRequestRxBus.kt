package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.BloodPressureMetaRequest


class BloodPressureMetaRequestRxBus private constructor() {

    private val relay: PublishRelay<BloodPresureMetaRequestResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, bloodPressureMetaRequest: BloodPressureMetaRequest) {
        relay.accept(BloodPresureMetaRequestResponse(eventType, bloodPressureMetaRequest))
    }

    fun toObservable(): Observable<BloodPresureMetaRequestResponse> {
        return relay
    }

    companion object {

        private var instance: BloodPressureMetaRequestRxBus? = null

        @Synchronized
        fun getInstance(): BloodPressureMetaRequestRxBus {
            if (instance == null) {
                instance = BloodPressureMetaRequestRxBus()
            }
            return instance as BloodPressureMetaRequestRxBus
        }
    }
}