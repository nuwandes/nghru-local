package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.HouseholdRequest

class SyncHouseholdRxBus private constructor() {
    private val relay: PublishRelay<SyncHouseholdResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, comment: HouseholdRequest) {
        relay.accept(SyncHouseholdResponse(eventType, comment))
    }

    fun toObservable(): Observable<SyncHouseholdResponse> {
        return relay
    }

    companion object {

        private var instance: SyncHouseholdRxBus? = null

        @Synchronized
        fun getInstance(): SyncHouseholdRxBus {
            if (instance == null) {
                instance = SyncHouseholdRxBus()
            }
            return instance as SyncHouseholdRxBus
        }
    }
}