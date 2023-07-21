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


/**
 * Created by shanuka on 10/26/17.
 */
@Entity(tableName = "access_token")
data class AccessToken(

    @SerializedName(value = "expires_in")
    @Expose @ColumnInfo(name = "expires_in")
    var expiresIn: String? = null,
    @SerializedName(value = "token_type")
    @ColumnInfo(name = "token_type")
    @Expose var tokenType: String? = null,
    @SerializedName(value = "refresh_token")
    @ColumnInfo(name = "refresh_token")
    @Expose var refreshToken: String? = null,
    @SerializedName(value = "access_token")
    @ColumnInfo(name = "access_token")
    @Expose var accessToken: String? = null
) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @SerializedName(value = "user_name")
    @ColumnInfo(name = "user_name")
    lateinit var userName: String
    @SerializedName(value = "password")
    @ColumnInfo(name = "password")
    lateinit var passwordEN: String
    @SerializedName(value = "status")
    @ColumnInfo(name = "status")
    var status: Boolean = true

    @Ignore
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
        id = parcel.readInt()
        userName = parcel.readString()!!
        passwordEN = parcel.readString()!!
        status = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(expiresIn)
        parcel.writeString(tokenType)
        parcel.writeString(refreshToken)
        parcel.writeString(accessToken)
        parcel.writeInt(id)
        parcel.writeString(userName)
        parcel.writeString(passwordEN)
        parcel.writeByte(if (status) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AccessToken> {
        override fun createFromParcel(parcel: Parcel): AccessToken {
            return AccessToken(parcel)
        }

        override fun newArray(size: Int): Array<AccessToken?> {
            return arrayOfNulls(size)
        }
    }

}
