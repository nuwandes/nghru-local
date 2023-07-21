package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Devices(
    @field:SerializedName("ips")
    var deviceIPs: List<String>
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(parcel.createStringArrayList()!!) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(deviceIPs)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Devices> {
        override fun createFromParcel(parcel: Parcel): Devices {
            return Devices(parcel)
        }

        override fun newArray(size: Int): Array<Devices?> {
            return arrayOfNulls(size)
        }
    }
}
