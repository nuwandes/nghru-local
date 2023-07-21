package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Meta
import java.io.Serializable


@Entity(
    tableName = "blood_pressure_meta"
)
data class BloodPressureMetaRequest(
    @Expose @field:SerializedName("meta")
    @Embedded(prefix = "meta")
    var meta: Meta,
    @Expose @field:SerializedName("body")
    @Embedded(prefix = "body")
    var body: BloodPresureRequest
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @ColumnInfo(name = "blood_pressure_request_id")
    var bloodPresureRequestId: Long = 0




    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Meta::class.java.classLoader)!!,
        parcel.readParcelable(BloodPresureRequest::class.java.classLoader)!!
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        bloodPresureRequestId = parcel.readLong()

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(meta, flags)
        parcel.writeParcelable(body, flags)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeLong(bloodPresureRequestId)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantMeta> {
        override fun createFromParcel(parcel: Parcel): ParticipantMeta {
            return ParticipantMeta(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantMeta?> {
            return arrayOfNulls(size)
        }
    }


}