package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.HouseholdRequest

class SyncHouseholdRequestRxBus private constructor() {
    private val relay: PublishRelay<SyncHouseholdRequestResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, comment: HouseholdRequest) {
        relay.accept(SyncHouseholdRequestResponse(eventType, comment))
    }

    fun toObservable(): Observable<SyncHouseholdRequestResponse> {
        return relay
    }

    companion object {

        private var instance: SyncHouseholdRequestRxBus? = null

        @Synchronized
        fun getInstance(): SyncHouseholdRequestRxBus {
            if (instance == null) {
                instance = SyncHouseholdRequestRxBus()
            }
            return instance as SyncHouseholdRequestRxBus
        }
    }
}