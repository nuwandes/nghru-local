package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(primaryKeys = ["id"])
data class User(
    @SerializedName(value = "id")
    @Expose @ColumnInfo(name = "id") val id: String,
    @SerializedName(value = "name")
    @Expose @ColumnInfo(name = "name") val name: String?,
    @SerializedName(value = "email")
    @Expose @ColumnInfo(name = "email") val email: String?,
    @SerializedName(value = "mobile")
    @Expose @ColumnInfo(name = "mobile") val mobile: String?,
    @SerializedName(value = "team")
    @Expose @Embedded(prefix = "team_") val team: Team?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Team::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(mobile)
        parcel.writeParcelable(team, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}


@Entity(primaryKeys = ["id"])
data class Team(
    @SerializedName(value = "id")
    @Expose @ColumnInfo(name = "id") val id: String,
    @SerializedName(value = "name")
    @Expose @ColumnInfo(name = "name") val name: String?,
    @SerializedName(value = "description")
    @Expose @ColumnInfo(name = "description") val description: String?,
    @SerializedName(value = "country")
    @Expose @ColumnInfo(name = "country") val country: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(country)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Team> {
        override fun createFromParcel(parcel: Parcel): Team {
            return Team(parcel)
        }

        override fun newArray(size: Int): Array<Team?> {
            return arrayOfNulls(size)
        }
    }

}





