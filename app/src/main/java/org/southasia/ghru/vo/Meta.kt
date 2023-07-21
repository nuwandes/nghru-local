package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "meta_new")
data class Meta(
    @Expose @field:SerializedName("collected_by")
    @ColumnInfo(name = "collected_by")
    var collectedBy: String?,
    @Expose @field:SerializedName("start_time")
    @ColumnInfo(name = "start_time")
    var startTime: String?
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @Expose
    @field:SerializedName("end_time")
    @ColumnInfo(name = "end_time")
    var endTime: String? = null
    @Expose
    @field:SerializedName("registered_by")
    @ColumnInfo(name = "registered_by")
    var registeredBy: String? = null

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
        id = parcel.readLong()
        endTime = parcel.readString()
        registeredBy = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(collectedBy)
        parcel.writeString(startTime)
        parcel.writeLong(id)
        parcel.writeString(endTime)
        parcel.writeString(registeredBy)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Meta> {
        override fun createFromParcel(parcel: Parcel): Meta {
            return Meta(parcel)
        }

        override fun newArray(size: Int): Array<Meta?> {
            return arrayOfNulls(size)
        }
    }


}
