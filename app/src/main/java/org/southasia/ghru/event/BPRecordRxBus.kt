package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.BloodPressure


class BPRecordRxBus private constructor() {

    private val relay: PublishRelay<BloodPressure>
    private val relayReset: PublishRelay<Int>

    init {
        relayReset = PublishRelay.create()
    }

    init {
        relay = PublishRelay.create()
    }

    fun post(record: BloodPressure) {
        relay.accept(record)
    }

    fun post(record: Int) {
        relayReset.accept(record)
    }

    fun toObservable(): Observable<BloodPressure> {
        return relay
    }

    fun toObservableReset(): Observable<Int> {
        return relayReset
    }

    companion object {

        private var instance: BPRecordRxBus? = null

        @Synchronized
        fun getInstance(): BPRecordRxBus {
            if (instance == null) {
                instance = BPRecordRxBus()
            }
            return instance as BPRecordRxBus
        }
    }
}