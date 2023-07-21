package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    indices = [
        (Index("id")),
        (Index("user_id"))],
    tableName = "login_data"
)
data class LoginData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @field:SerializedName("user")
    @field:Embedded(prefix = "user_")
    val user: User,
    @field:SerializedName("token")
    val token: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        TODO("user"),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(token)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoginData> {
        override fun createFromParcel(parcel: Parcel): LoginData {
            return LoginData(parcel)
        }

        override fun newArray(size: Int): Array<LoginData?> {
            return arrayOfNulls(size)
        }
    }

}