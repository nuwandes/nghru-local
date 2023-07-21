package org.southasia.ghru.vo

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.southasia.ghru.BR
import java.io.Serializable


class Storage : BaseObservable(), Serializable {

    companion object {
        fun build(): Storage {
            val hb1Ac = Storage()
            hb1Ac.freezerId = String()
            return hb1Ac
        }
    }


    var freezerId: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.value)
        }
        @Bindable get() = field
}
