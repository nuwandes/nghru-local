package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.room.Entity
import androidx.room.Index


class BloodPressure(var id: Int) : Parcelable {

    var systolic: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var diastolic: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var pulse: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var arm: MutableLiveData<String> = MutableLiveData<String>().apply { }

    constructor(parcel: Parcel) : this(parcel.readInt()) {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BloodPressure> {
        override fun createFromParcel(parcel: Parcel): BloodPressure {
            return BloodPressure(parcel)
        }

        override fun newArray(size: Int): Array<BloodPressure?> {
            return arrayOfNulls(size)
        }
    }

}


enum class Arm(val arm: String) {
    LEFT("Left"),
    RIGHT("Right")
}



