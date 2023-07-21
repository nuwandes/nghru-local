package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "sample_storage_request")
data class SampleStorageRequest(
    @Expose var status: Int,
    @Expose @SerializedName("storage_id") @ColumnInfo(name = "storage_id") val storageId: String,
    @SerializedName("sampleId") @ColumnInfo(name = "sample_id") val sampleId: String
) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var timestamp: Long = System.currentTimeMillis()
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(status)
        parcel.writeString(storageId)
        parcel.writeString(sampleId)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SampleStorageRequest> {
        override fun createFromParcel(parcel: Parcel): SampleStorageRequest {
            return SampleStorageRequest(parcel)
        }

        override fun newArray(size: Int): Array<SampleStorageRequest?> {
            return arrayOfNulls(size)
        }
    }

}