package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.FastingBloodGlucoseDto

class FastingBloodGlucoseRxBus private constructor() {
    private val relay: PublishRelay<FastingBloodGlucoseDto>

    init {
        relay = PublishRelay.create()
    }

    fun post(hb1Ac: FastingBloodGlucoseDto) {
        relay.accept(hb1Ac)
    }

    fun toObservable(): Observable<FastingBloodGlucoseDto> {
        return relay
    }

    companion object {

        private var instance: FastingBloodGlucoseRxBus? = null

        @Synchronized
        fun getInstance(): FastingBloodGlucoseRxBus {
            if (instance == null) {
                instance = FastingBloodGlucoseRxBus()
            }
            return instance as FastingBloodGlucoseRxBus
        }
    }
}