package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class HomeItem(
    var id: Int = 0,
    var name: String,
    var resourceId: Int = 0
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(resourceId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeItem> {
        override fun createFromParcel(parcel: Parcel): HomeItem {
            return HomeItem(parcel)
        }

        override fun newArray(size: Int): Array<HomeItem?> {
            return arrayOfNulls(size)
        }
    }

}
