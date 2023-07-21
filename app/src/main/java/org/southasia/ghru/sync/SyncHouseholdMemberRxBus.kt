package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.request.Member

class SyncHouseholdMemberRxBus private constructor() {
    private val relay: PublishRelay<SyncHouseholdMemberResponse>

    init {
        relay = PublishRelay.create()
    }

    fun post(eventType: SyncResponseEventType, member: Member) {
        relay.accept(SyncHouseholdMemberResponse(eventType, member))
    }

    fun toObservable(): Observable<SyncHouseholdMemberResponse> {
        return relay
    }

    companion object {

        private var instance: SyncHouseholdMemberRxBus? = null

        @Synchronized
        fun getInstance(): SyncHouseholdMemberRxBus {
            if (instance == null) {
                instance = SyncHouseholdMemberRxBus()
            }
            return instance as SyncHouseholdMemberRxBus
        }
    }
}