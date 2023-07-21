package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CommonResponce(
    @Expose @field:SerializedName("error") var error: String?,
    @Expose @field:SerializedName("data") var data: CommonResponceData?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(CommonResponceData::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(error)
        parcel.writeParcelable(data, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommonResponce> {
        override fun createFromParcel(parcel: Parcel): CommonResponce {
            return CommonResponce(parcel)
        }

        override fun newArray(size: Int): Array<CommonResponce?> {
            return arrayOfNulls(size)
        }
    }

}

data class CommonResponceData(
    @Expose @field:SerializedName("data") var message: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommonResponceData> {
        override fun createFromParcel(parcel: Parcel): CommonResponceData {
            return CommonResponceData(parcel)
        }

        override fun newArray(size: Int): Array<CommonResponceData?> {
            return arrayOfNulls(size)
        }
    }

}
