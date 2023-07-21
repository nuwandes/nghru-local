package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "body_measurement_request")
data class BodyMeasurementRequest(
    @Expose @SerializedName("height") @Embedded(prefix = "height") val height: BodyMeasurementItemRequest,
    @Expose @SerializedName("weight") @Embedded(prefix = "weight") val weight: BodyMeasurementItemRequest,
    @Expose @SerializedName("fat_composition") @Embedded(prefix = "fat_composition") val fatComposition: BodyMeasurementItemRequest,
    @Expose @SerializedName("hip_size") @Embedded(prefix = "hip_size") val hipSize: BodyMeasurementItemRequest,
    @Expose @SerializedName("waist_size") @Embedded(prefix = "waist_size") val waistSize: BodyMeasurementItemRequest,
    @Expose @SerializedName("visceral") @Embedded(prefix = "visceral") val visceralFat: BodyMeasurementItemRequest,
    @Expose @SerializedName("muscle") @Embedded(prefix = "muscle") val muscle: BodyMeasurementItemRequest

) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Ignore
    @Expose
    @SerializedName("blood_pressure")
    var bloodPresureRequestList: List<BloodPresureItemRequest>? = null

    var timestamp: Long = System.currentTimeMillis()
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(BodyMeasurementItemRequest::class.java.classLoader)!!,
        parcel.readParcelable(BodyMeasurementItemRequest::class.java.classLoader)!!,
        parcel.readParcelable(BodyMeasurementItemRequest::class.java.classLoader)!!,
        parcel.readParcelable(BodyMeasurementItemRequest::class.java.classLoader)!!,
        parcel.readParcelable(BodyMeasurementItemRequest::class.java.classLoader)!!,
        parcel.readParcelable(BodyMeasurementItemRequest::class.java.classLoader)!!,
        parcel.readParcelable(BodyMeasurementItemRequest::class.java.classLoader)!!
    ) {
        id = parcel.readLong()
        bloodPresureRequestList = parcel.createTypedArrayList(BloodPresureItemRequest)
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(height, flags)
        parcel.writeParcelable(weight, flags)
        parcel.writeParcelable(fatComposition, flags)
        parcel.writeParcelable(hipSize, flags)
        parcel.writeParcelable(waistSize, flags)
        parcel.writeParcelable(visceralFat, flags)
        parcel.writeParcelable(muscle, flags)
        parcel.writeLong(id)
        parcel.writeTypedList(bloodPresureRequestList)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BodyMeasurementRequest> {
        override fun createFromParcel(parcel: Parcel): BodyMeasurementRequest {
            return BodyMeasurementRequest(parcel)
        }

        override fun newArray(size: Int): Array<BodyMeasurementRequest?> {
            return arrayOfNulls(size)
        }
    }
}