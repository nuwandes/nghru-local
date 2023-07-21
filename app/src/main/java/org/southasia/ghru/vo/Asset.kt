package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "asset")
data class Asset(
    @Expose @field:SerializedName("id")
    @ColumnInfo(name = "id")
    var id: String,
    @Expose @field:SerializedName("url")
    @ColumnInfo(name = "url")
    var url: String,
    @Expose @field:SerializedName("subject_id")
    @ColumnInfo(name = "subject_id")
    var subjectId: String,
    @Expose @field:SerializedName("subject_type")
    @ColumnInfo(name = "subject_type")
    val subjectType: String,
    @Expose @field:SerializedName("purpose")
    @ColumnInfo(name = "purpose")
    val purpose: String,
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false
    //@ColumnInfo(name = "sync_pending")
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "assret_id")
    var assetid: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    ) {
        assetid = parcel.readLong()
        timestamp = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(url)
        parcel.writeString(subjectId)
        parcel.writeString(subjectType)
        parcel.writeString(purpose)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeLong(assetid)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Asset> {
        override fun createFromParcel(parcel: Parcel): Asset {
            return Asset(parcel)
        }

        override fun newArray(size: Int): Array<Asset?> {
            return arrayOfNulls(size)
        }
    }

}
