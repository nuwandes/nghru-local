package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.SampleProcess

class SyncSampleProcessRxBus private constructor() {
    private val relay: PublishRelay<SyncSampleProcessResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, sampleProcess: SampleProcess) {
        relay.accept(SyncSampleProcessResponse(eventType, sampleProcess))
    }

    fun toObservable(): Observable<SyncSampleProcessResponse> {
        return relay
    }

    companion object {

        private var instance: SyncSampleProcessRxBus? = null

        @Synchronized
        fun getInstance(): SyncSampleProcessRxBus {
            if (instance == null) {
                instance = SyncSampleProcessRxBus()
            }
            return instance as SyncSampleProcessRxBus
        }
    }
}