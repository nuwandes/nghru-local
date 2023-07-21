package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.HouseholdRequestMeta

class SyncHouseholdRequestmetaRxBus private constructor() {
    private val relay: PublishRelay<SyncHouseholdrequestmetaResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, householdRequestMeta: HouseholdRequestMeta) {
        relay.accept(SyncHouseholdrequestmetaResponse(eventType, householdRequestMeta))
    }

    fun toObservable(): Observable<SyncHouseholdrequestmetaResponse> {
        return relay
    }

    companion object {

        private var instance: SyncHouseholdRequestmetaRxBus? = null

        @Synchronized
        fun getInstance(): SyncHouseholdRequestmetaRxBus {
            if (instance == null) {
                instance = SyncHouseholdRequestmetaRxBus()
            }
            return instance as SyncHouseholdRequestmetaRxBus
        }
    }
}