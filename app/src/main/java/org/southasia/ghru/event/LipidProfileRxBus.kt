package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.LipidProfileDto

class LipidProfileRxBus private constructor() {
    private val relay: PublishRelay<LipidProfileDto>

    init {
        relay = PublishRelay.create()
    }

    fun post(hb1Ac: LipidProfileDto) {
        relay.accept(hb1Ac)
    }

    fun toObservable(): Observable<LipidProfileDto> {
        return relay
    }

    companion object {

        private var instance: LipidProfileRxBus? = null

        @Synchronized
        fun getInstance(): LipidProfileRxBus {
            if (instance == null) {
                instance = LipidProfileRxBus()
            }
            return instance as LipidProfileRxBus
        }
    }
}