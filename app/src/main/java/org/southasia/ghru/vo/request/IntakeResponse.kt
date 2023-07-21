package org.southasia.ghru.vo.request


import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Meta
import java.io.Serializable

data class IntakeResponse(
    @Expose @SerializedName("participant_id") val participant_id: String,
    @Expose @SerializedName("station_id") val station_id: String,
    @Expose @SerializedName("station_name") val station_name: String,
    @Expose @SerializedName("status_text") val status_text: String,
    @Expose @SerializedName("status_code") val status_code: String,
    @Expose @SerializedName("is_cancelled") val is_cancelled: String,
    @Expose @SerializedName("comment") val comment: String,
    @Expose @SerializedName("reason") val reason: String,
    @Expose @SerializedName("device_id") val device_id: String,
    @Expose @SerializedName("intake_url") val intake_url: String

) : Serializable, Parcelable {

    @Ignore
    var timestamp: Long = System.currentTimeMillis()

    @Ignore
    var meta: Meta? = null

    @Ignore
    var createdDateTime: String = ""

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {

        timestamp = parcel.readLong()
        createdDateTime = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(participant_id)
        parcel.writeString(station_id)
        parcel.writeString(station_name)
        parcel.writeString(status_text)
        parcel.writeString(status_code)
        parcel.writeString(is_cancelled)
        parcel.writeString(comment)
        parcel.writeString(reason)
        parcel.writeString(device_id)
        parcel.writeString(intake_url)
        parcel.writeLong(timestamp)
        parcel.writeString(createdDateTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantRequest> {
        override fun createFromParcel(parcel: Parcel): ParticipantRequest {
            return ParticipantRequest(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantRequest?> {
            return arrayOfNulls(size)
        }
    }

}