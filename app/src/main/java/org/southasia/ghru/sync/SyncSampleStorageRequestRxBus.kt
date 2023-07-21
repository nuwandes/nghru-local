package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.SampleStorageRequest

class SyncSampleStorageRequestRxBus private constructor() {
    private val relay: PublishRelay<SyncSampleStorageRequestResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, sampleRequest: SampleStorageRequest) {
        relay.accept(SyncSampleStorageRequestResponse(eventType, sampleRequest))
    }

    fun toObservable(): Observable<SyncSampleStorageRequestResponse> {
        return relay
    }

    companion object {

        private var instance: SyncSampleStorageRequestRxBus? = null

        @Synchronized
        fun getInstance(): SyncSampleStorageRequestRxBus {
            if (instance == null) {
                instance = SyncSampleStorageRequestRxBus()
            }
            return instance as SyncSampleStorageRequestRxBus
        }
    }
}