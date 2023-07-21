package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TriglyceridesDto(
    @Expose @field:SerializedName("lot_id") var lot_id: String?,
    @Expose @field:SerializedName("value") var value: String?,
    @Expose @field:SerializedName("device_id") var device_id: String?,
    @Expose @field:SerializedName("comment") var comment: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(lot_id)
        parcel.writeString(value)
        parcel.writeString(device_id)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TriglyceridesDto> {
        override fun createFromParcel(parcel: Parcel): TriglyceridesDto {
            return TriglyceridesDto(parcel)
        }

        override fun newArray(size: Int): Array<TriglyceridesDto?> {
            return arrayOfNulls(size)
        }
    }

}