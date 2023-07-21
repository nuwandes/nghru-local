package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.Hb1AcDto

class Hb1AcRxBus private constructor() {
    private val relay: PublishRelay<Hb1AcDto>

    init {
        relay = PublishRelay.create()
    }

    fun post(hb1Ac: Hb1AcDto) {
        relay.accept(hb1Ac)
    }

    fun toObservable(): Observable<Hb1AcDto> {
        return relay
    }

    companion object {

        private var instance: Hb1AcRxBus? = null

        @Synchronized
        fun getInstance(): Hb1AcRxBus {
            if (instance == null) {
                instance = Hb1AcRxBus()
            }
            return instance as Hb1AcRxBus
        }
    }
}