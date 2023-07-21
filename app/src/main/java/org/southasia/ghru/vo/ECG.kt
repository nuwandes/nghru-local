package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ECG(
    @Expose @field:SerializedName("patient_id") var patientId: String?,
    @Expose @field:SerializedName("station_id") var stationId: String?,
    @Expose @field:SerializedName("station_name") var stationName: String?,
    @Expose @field:SerializedName("status_text") var statusText: String?,
    @Expose @field:SerializedName("status_code") var statusCode: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(patientId)
        parcel.writeString(stationId)
        parcel.writeString(stationName)
        parcel.writeString(statusText)
        parcel.writeString(statusCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ECG> {
        override fun createFromParcel(parcel: Parcel): ECG {
            return ECG(parcel)
        }

        override fun newArray(size: Int): Array<ECG?> {
            return arrayOfNulls(size)
        }
    }

}
