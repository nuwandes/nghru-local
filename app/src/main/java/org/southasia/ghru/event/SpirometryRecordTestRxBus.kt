package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.SpirometryRecord

class SpirometryRecordTestRxBus private constructor() {

    private val relay: PublishRelay<SpirometryRecord>

    init {
        relay = PublishRelay.create()
    }

    fun post(record: SpirometryRecord) {
        relay.accept(record)
    }

    fun toObservable(): Observable<SpirometryRecord> {
        return relay
    }

    companion object {

        private var instance: SpirometryRecordTestRxBus? = null

        @Synchronized
        fun getInstance(): SpirometryRecordTestRxBus {
            if (instance == null) {
                instance = SpirometryRecordTestRxBus()
            }
            return instance as SpirometryRecordTestRxBus
        }
    }
}