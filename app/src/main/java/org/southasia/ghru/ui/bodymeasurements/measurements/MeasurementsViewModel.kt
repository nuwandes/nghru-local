package org.southasia.ghru.ui.bodymeasurements.measurements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.southasia.ghru.vo.BodyMeasurement
import javax.inject.Inject


class MeasurementsViewModel
@Inject constructor() : ViewModel() {

    var bodyMeasurement: MutableLiveData<BodyMeasurement>? = null

    var isValidHeight: Boolean = false

    var isValidWeight: Boolean = false

    var isValifFatComp: Boolean = false

    var isValidVisceralFat: Boolean = false

    var isValidMuscle: Boolean = false

    var isValidHipSize: Boolean = false

    var isValidWaistSize: Boolean = false


    fun getBodyMeasurement(): LiveData<BodyMeasurement> {
        if (bodyMeasurement == null) {
            bodyMeasurement = MutableLiveData<BodyMeasurement>()
            loadBodyMeasurement()
        }
        return bodyMeasurement as LiveData<BodyMeasurement>
    }

    fun loadBodyMeasurement() {
        bodyMeasurement?.value = BodyMeasurement()
    }


}


