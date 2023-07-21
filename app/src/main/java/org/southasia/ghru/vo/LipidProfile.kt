package org.southasia.ghru.vo

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.southasia.ghru.BR
import java.io.Serializable


class LipidProfile : BaseObservable(), Serializable {

    companion object {
        fun build(): LipidProfile {
            val hb1Ac = LipidProfile()
            hb1Ac.probeId = String()
            hb1Ac.totalCholesterol = String()
            hb1Ac.hDL = String()
            hb1Ac.triglycerol = String()
            hb1Ac.lDLC = String()

            return hb1Ac
        }
    }


    var totalCholesterol: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.value)
        }
        @Bindable get() = field


    var triglycerol: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.probeId)
        }
        @Bindable get() = field


    var probeId: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.probeId)
        }
        @Bindable get() = field


    var lDLC: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.probeId)
        }
        @Bindable get() = field


    var hDL: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.probeId)
        }
        @Bindable get() = field


}
