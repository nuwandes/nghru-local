package org.southasia.ghru.vo

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.southasia.ghru.BR
import java.io.Serializable

class Hemoglobin : BaseObservable(), Serializable {

    companion object {
        fun build(): Hemoglobin {
            val hemo = Hemoglobin()
            hemo.probeId = String()
            hemo.value = String()
            hemo.comment = String()
            hemo.deviceId = String()
            return hemo
        }
    }

    var value: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.value)
        }
        @Bindable get() = field


    var probeId: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.probeId)
        }
        @Bindable get() = field


    var comment: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.comment)
        }
        @Bindable get() = field


    var deviceId: String = String()
        set(value) {
            field = value
        }
        @Bindable get() = field
}