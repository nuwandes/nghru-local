package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.SampleRequest

class SyncSampleRequestRxBus private constructor() {
    private val relay: PublishRelay<SyncSampleRequestResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, sampleRequest: SampleRequest) {
        relay.accept(SyncSampleRequestResponse(eventType, sampleRequest))
    }

    fun toObservable(): Observable<SyncSampleRequestResponse> {
        return relay
    }

    companion object {

        private var instance: SyncSampleRequestRxBus? = null

        @Synchronized
        fun getInstance(): SyncSampleRequestRxBus {
            if (instance == null) {
                instance = SyncSampleRequestRxBus()
            }
            return instance as SyncSampleRequestRxBus
        }
    }
}