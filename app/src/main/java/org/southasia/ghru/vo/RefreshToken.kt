package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RefreshToken(
    @Expose @SerializedName("refresh_token") @ColumnInfo(name = "refresh_token") var refresh_token: String
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(refresh_token)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RefreshToken> {
        override fun createFromParcel(parcel: Parcel): RefreshToken {
            return RefreshToken(parcel)
        }

        override fun newArray(size: Int): Array<RefreshToken?> {
            return arrayOfNulls(size)
        }
    }

}
