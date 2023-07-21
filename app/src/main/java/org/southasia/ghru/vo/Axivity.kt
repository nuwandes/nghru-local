package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.request.BloodPresureItemRequest
import java.io.Serializable


@Entity(tableName = "axivity")
data class Axivity(
    @Expose @field:SerializedName("session_id") @ColumnInfo(name = "session_id") var sessionId: String?,
    @Expose @field:SerializedName("comment") @ColumnInfo(name = "comment") var comment: String?,
    @Expose @field:SerializedName("dominant_wrist") @ColumnInfo(name = "dominantWrist") var dominantWrist: String?,
    @Expose @field:SerializedName("start_time") @ColumnInfo(name = "startTime") var startTime: String?,
    @Expose @field:SerializedName("end_time") @ColumnInfo(name = "endTime") var endTime: String?,
    @Expose @field:SerializedName("serial_id") @ColumnInfo(name = "serialNumber") var serialNumber: String?

) : Serializable, Parcelable {

    @Expose @field:SerializedName("meta")
    @Embedded(prefix = "meta")
    var meta: Meta? = null

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @ColumnInfo(name = "screening_id")
    lateinit var screeningId: String

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()

    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        screeningId  = parcel.readString()

    }

    constructor(sessionId: String?, startTime: String?, endTime: String?, serialNumber: String?) : this(
        sessionId = sessionId,
        dominantWrist = null,
        startTime = startTime,
        endTime = endTime,
        comment = null,
        serialNumber = serialNumber
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sessionId)
        parcel.writeString(comment)
        parcel.writeString(dominantWrist)
        parcel.writeString(startTime)
        parcel.writeString(endTime)
        parcel.writeString(serialNumber)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeString(screeningId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Axivity> {
        override fun createFromParcel(parcel: Parcel): Axivity {
            return Axivity(parcel)
        }

        override fun newArray(size: Int): Array<Axivity?> {
            return arrayOfNulls(size)
        }
    }

}
