package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Entity(tableName = "sample_data")
data class SampleData(
    @Embedded @field:SerializedName("sample") val sample: Sample?,
    @Embedded @field:SerializedName("station") val station: Station?
) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var timestamp: Long = System.currentTimeMillis()
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Sample::class.java.classLoader),
        parcel.readParcelable(Station::class.java.classLoader)
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(sample, flags)
        parcel.writeParcelable(station, flags)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SampleData> {
        override fun createFromParcel(parcel: Parcel): SampleData {
            return SampleData(parcel)
        }

        override fun newArray(size: Int): Array<SampleData?> {
            return arrayOfNulls(size)
        }
    }

}