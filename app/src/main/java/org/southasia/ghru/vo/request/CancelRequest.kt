package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "cancel_request")
data class CancelRequest(
    @Expose @SerializedName("station_type") @ColumnInfo(name = "station_type") var stationType: String?,
    @Expose @SerializedName("comment")@ColumnInfo(name = "comment") var comment: String?,
    @Expose @SerializedName("reason")@ColumnInfo(name = "reason") var reason: String?

) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var timestamp: Long = System.currentTimeMillis()

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @ColumnInfo(name = "screening_id")
    lateinit var screeningId: String

    @ColumnInfo(name = "created_date_time")
    lateinit var createdDateTime: String

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        screeningId = parcel.readString()
        createdDateTime = parcel.readString()
    }

    @Ignore
    constructor() : this(reason = null, stationType = null, comment = null)

    @Ignore
    constructor(stationType: String) : this(reason = null, stationType = stationType, comment = null)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(stationType)
        parcel.writeString(comment)
        parcel.writeString(reason)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeString(screeningId)
        parcel.writeString(createdDateTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CancelRequest> {
        override fun createFromParcel(parcel: Parcel): CancelRequest {
            return CancelRequest(parcel)
        }

        override fun newArray(size: Int): Array<CancelRequest?> {
            return arrayOfNulls(size)
        }
    }


}