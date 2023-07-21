package org.southasia.ghru.sync

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.Member

class SyncMemberRxBus private constructor() {
    private val relay: PublishRelay<Member>

    init {
        relay = PublishRelay.create()
    }

    fun post(member: Member) {
        relay.accept(member)
    }

    fun toObservable(): Observable<Member> {
        return relay
    }

    companion object {

        private var instance: SyncMemberRxBus? = null

        @Synchronized
        fun getInstance(): SyncMemberRxBus {
            if (instance == null) {
                instance = SyncMemberRxBus()
            }
            return instance as SyncMemberRxBus
        }
    }
}