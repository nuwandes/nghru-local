package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "station_devices")
data class StationDeviceData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @Expose @SerializedName("id") @ColumnInfo(name = "device_id") val device_id: String,
    @Expose @SerializedName("device_name") @ColumnInfo(name = "device_name") val device_name: String?,
    @Expose @SerializedName("station") @ColumnInfo(name = "station") val station: String?,
    @Expose @SerializedName("measurement") @ColumnInfo(name = "measurement") val measurement: String?,
    @Expose @SerializedName("status") @ColumnInfo(name = "status") val status: Boolean

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(device_id)
        parcel.writeString(device_name)
        parcel.writeString(station)
        parcel.writeString(measurement)
        parcel.writeByte(if (status) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginData> {
        override fun createFromParcel(parcel: Parcel): LoginData {
            return LoginData(parcel)
        }

        override fun newArray(size: Int): Array<LoginData?> {
            return arrayOfNulls(size)
        }
    }

}
