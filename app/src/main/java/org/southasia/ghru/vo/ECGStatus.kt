package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Entity(tableName = "ecg_status")
data class ECGStatus(
    @Expose @field:SerializedName("trace_status") @ColumnInfo(name = "trace_status") var traceStatus: String?,
    @Expose @field:SerializedName("comment") @ColumnInfo(name = "comment") var comment: String?,
    @Expose @field:SerializedName("device_id") @ColumnInfo(name = "device_id") var device_id: String?,
    @Expose @field:SerializedName("meta") @Embedded(prefix = "meta") var meta: Meta?
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @ColumnInfo(name = "meta_id")
    var metaId: Long = 0

    @ColumnInfo(name = "screening_id")
    lateinit var screeningId: String

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Meta::class.java.classLoader)
    ) {
        id = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        metaId = parcel.readLong()
        screeningId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(traceStatus)
        parcel.writeString(comment)
        parcel.writeString(device_id)
        parcel.writeParcelable(meta, flags)

        parcel.writeLong(id)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeLong(metaId)
        parcel.writeString(screeningId)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ECGStatus> {
        override fun createFromParcel(parcel: Parcel): ECGStatus {
            return ECGStatus(parcel)
        }

        override fun newArray(size: Int): Array<ECGStatus?> {
            return arrayOfNulls(size)
        }
    }

}
