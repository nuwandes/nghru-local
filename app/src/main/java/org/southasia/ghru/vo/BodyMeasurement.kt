package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData

class BodyMeasurement() : Parcelable {
    var height: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var weight: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var fatComposition: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var hipSize: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var waistSize: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var bloodPressures =
        MutableLiveData<ArrayList<BloodPressure>>().apply { ArrayList<BloodPressure>() } //{ ArrayList<BloodPressure>(3, { i -> BloodPressure(0) }) }
    var skip: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var reson: MutableLiveData<String> = MutableLiveData<String>().apply { }

    var visceralFat: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var muscle: MutableLiveData<String> = MutableLiveData<String>().apply { }

    constructor(parcel: Parcel) : this() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BodyMeasurement> {
        override fun createFromParcel(parcel: Parcel): BodyMeasurement {
            return BodyMeasurement(parcel)
        }

        override fun newArray(size: Int): Array<BodyMeasurement?> {
            return arrayOfNulls(size)
        }
    }

}