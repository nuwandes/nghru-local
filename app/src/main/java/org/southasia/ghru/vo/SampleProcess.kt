package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "sample_process")
data class SampleProcess(
    @Expose var status: Int, @Expose var data: String, @Expose @SerializedName("storage_id") @ColumnInfo(
        name = "storage_id"
    ) val storageId: String
) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var timestamp: Long = System.currentTimeMillis()
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @Expose
    @SerializedName("meta")
    @Ignore
    var meta: Meta? = null

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        meta = parcel.readParcelable(Meta::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(status)
        parcel.writeString(data)
        parcel.writeString(storageId)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeParcelable(meta, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SampleProcess> {
        override fun createFromParcel(parcel: Parcel): SampleProcess {
            return SampleProcess(parcel)
        }

        override fun newArray(size: Int): Array<SampleProcess?> {
            return arrayOfNulls(size)
        }
    }


}
