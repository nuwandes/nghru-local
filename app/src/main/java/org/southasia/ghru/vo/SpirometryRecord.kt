package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import java.io.Serializable

class SpirometryRecord() : Serializable, Parcelable {

    var fev: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var fvc: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var ratio: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var pEFR: MutableLiveData<String> = MutableLiveData<String>().apply { }
    var skip: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) }
    var reason: MutableLiveData<String> = MutableLiveData<String>().apply { }

    constructor(parcel: Parcel) : this() {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "SpirometryRecord(fev=${fev.value}, fvc=${fvc.value}, ratio=${ratio.value}, skip=${skip.value}, reason=${reason.value})"
    }

    companion object CREATOR : Parcelable.Creator<SpirometryRecord> {
        override fun createFromParcel(parcel: Parcel): SpirometryRecord {
            return SpirometryRecord(parcel)
        }

        override fun newArray(size: Int): Array<SpirometryRecord?> {
            return arrayOfNulls(size)
        }
    }


}