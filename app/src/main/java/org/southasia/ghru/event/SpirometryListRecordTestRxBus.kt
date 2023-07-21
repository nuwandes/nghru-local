package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.SpirometryRecord

class SpirometryListRecordTestRxBus private constructor() {

    private val relay: PublishRelay<ArrayList<SpirometryRecord>>

    init {
        relay = PublishRelay.create()
    }

    fun post(record: ArrayList<SpirometryRecord>) {
        relay.accept(record)
    }

    fun toObservable(): Observable<ArrayList<SpirometryRecord>> {
        return relay
    }

    companion object {

        private var instance: SpirometryListRecordTestRxBus? = null

        @Synchronized
        fun getInstance(): SpirometryListRecordTestRxBus {
            if (instance == null) {
                instance = SpirometryListRecordTestRxBus()
            }
            return instance as SpirometryListRecordTestRxBus
        }
    }
}