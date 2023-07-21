package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Ignore
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Meta
import java.io.Serializable

class IntakeRequest(
    @Expose @SerializedName("comment") val comment: String
) : Serializable, Parcelable {

    var meta: Meta? = null

    @Expose @SerializedName("status")
    var status : String = ""

    @Ignore
    var createdDateTime: String = ""

    constructor(parcel: Parcel) : this(
        parcel.readString()!!

    ) {
        createdDateTime = parcel.readString()
        status = parcel.readString()
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comment)
        parcel.writeString(createdDateTime)
        parcel.writeString(status)
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