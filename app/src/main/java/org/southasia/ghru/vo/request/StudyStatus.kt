package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Entity(tableName = "study_status")
data class StudyStatus(
    @Expose @field:SerializedName("registered")
    @ColumnInfo(name = "registered")
    var registered: Boolean

) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    constructor(parcel: Parcel) : this(parcel.readByte() != 0.toByte()) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (registered) 1 else 0)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StudyStatus> {
        override fun createFromParcel(parcel: Parcel): StudyStatus {
            return StudyStatus(parcel)
        }

        override fun newArray(size: Int): Array<StudyStatus?> {
            return arrayOfNulls(size)
        }
    }


}
