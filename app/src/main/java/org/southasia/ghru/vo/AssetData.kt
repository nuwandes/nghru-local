package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AssetData() : Serializable, Parcelable {
    @SerializedName("data")
    internal var data: List<Asset>? = null
    @SerializedName("metadata")
    internal var metadata: Metadata? = null

    constructor(parcel: Parcel) : this() {
        data = parcel.createTypedArrayList(Asset)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AssetData> {
        override fun createFromParcel(parcel: Parcel): AssetData {
            return AssetData(parcel)
        }

        override fun newArray(size: Int): Array<AssetData?> {
            return arrayOfNulls(size)
        }
    }
}