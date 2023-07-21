package org.southasia.ghru.event

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.southasia.ghru.vo.TotalCholesterolDto

class TotalCholesterolRxBus private constructor() {
    private val relay: PublishRelay<TotalCholesterolDto>

    init {
        relay = PublishRelay.create()
    }

    fun post(totalCholesterolDto: TotalCholesterolDto) {
        relay.accept(totalCholesterolDto)
    }

    fun toObservable(): Observable<TotalCholesterolDto> {
        return relay
    }

    companion object {

        private var instance: TotalCholesterolRxBus? = null

        @Synchronized
        fun getInstance(): TotalCholesterolRxBus {
            if (instance == null) {
                instance = TotalCholesterolRxBus()
            }
            return instance as TotalCholesterolRxBus
        }
    }
}