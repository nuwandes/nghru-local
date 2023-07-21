package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Station(
    @Expose @field:SerializedName("station_id")
    @ColumnInfo(name = "station_id")
    var station_id: String,
    @Expose @field:SerializedName("station_name")
    @ColumnInfo(name = "station_name")
    var station_name: String,
    @Expose @field:SerializedName("patient_id")
    @ColumnInfo(name = "patient_id")
    var patient_id: String,
    @Expose @field:SerializedName("status_text")
    @ColumnInfo(name = "status_text")
    var status_text: String
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(station_id)
        parcel.writeString(station_name)
        parcel.writeString(patient_id)
        parcel.writeString(status_text)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Station> {
        override fun createFromParcel(parcel: Parcel): Station {
            return Station(parcel)
        }

        override fun newArray(size: Int): Array<Station?> {
            return arrayOfNulls(size)
        }
    }

}
