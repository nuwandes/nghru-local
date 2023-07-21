package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BodyMeasurementItemRequest(
    @Expose @SerializedName("value") @ColumnInfo(name = "value") val value: Double,
    @Expose @SerializedName("unit") @ColumnInfo(name = "unit") val unit: String
) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(value)
        parcel.writeString(unit)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BodyMeasurementItemRequest> {
        override fun createFromParcel(parcel: Parcel): BodyMeasurementItemRequest {
            return BodyMeasurementItemRequest(parcel)
        }

        override fun newArray(size: Int): Array<BodyMeasurementItemRequest?> {
            return arrayOfNulls(size)
        }
    }

}