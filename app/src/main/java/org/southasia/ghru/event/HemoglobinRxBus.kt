package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.HemoglobinDto

class HemoglobinRxBus private constructor() {
    private val relay: PublishRelay<HemoglobinDto>

    init {
        relay = PublishRelay.create()
    }

    fun post(hemoglobinDto: HemoglobinDto) {
        relay.accept(hemoglobinDto)
    }

    fun toObservable(): Observable<HemoglobinDto> {
        return relay
    }

    companion object {

        private var instance: HemoglobinRxBus? = null

        @Synchronized
        fun getInstance(): HemoglobinRxBus {
            if (instance == null) {
                instance = HemoglobinRxBus()
            }
            return instance as HemoglobinRxBus
        }
    }
}