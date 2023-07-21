package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    indices = [
        (Index("member_id"))], tableName = "member_data"
)
data class Member(
    @Expose @field:SerializedName("member_id")
    @ColumnInfo(name = "member_id")
    var memberId: String,
    @Expose @field:SerializedName("given_name")
    @ColumnInfo(name = "given_name")
    var givenName: String,
    @Expose @field:SerializedName("family_name")
    @ColumnInfo(name = "family_name")
    var familyName: String,
    @Expose @field:SerializedName("preferred_name")
    @ColumnInfo(name = "preferred_name")
    val preferredName: String,
    @Expose @field:SerializedName("contact_number")
    @ColumnInfo(name = "contact_number")
    var contactNumber: String,
    @Expose @field:SerializedName("contact_number_alternate")
    @ColumnInfo(name = "contact_number_alternate")
    val contactNumberAlternate: String,
    @Expose @field:SerializedName("gender")
    @ColumnInfo(name = "gender")
    var gender: String,
    @Expose @field:SerializedName("contact_no")
    @ColumnInfo(name = "contact_no")
    val position: String,
    @Expose @field:SerializedName("age")
    @ColumnInfo(name = "age")
    val age: String,
    @Expose @field:SerializedName("birth_date")
    @Embedded(prefix = "member_")
    var birthDate: Date,
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    var fathersName: String = String()
    var identityType: String = String()
    var identityImage: String = String()
    var profileImage: String = String()
    var householdId: String = String()
    var identityId: String = String()

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
        parcel.readParcelable(Date::class.java.classLoader)!!,
        parcel.readByte() != 0.toByte()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        fathersName = parcel.readString()!!
        identityType = parcel.readString()!!
        identityImage = parcel.readString()!!
        profileImage = parcel.readString()!!
        householdId = parcel.readString()!!
        identityId = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(memberId)
        parcel.writeString(givenName)
        parcel.writeString(familyName)
        parcel.writeString(preferredName)
        parcel.writeString(contactNumber)
        parcel.writeString(contactNumberAlternate)
        parcel.writeString(gender)
        parcel.writeString(position)
        parcel.writeString(age)
        parcel.writeParcelable(birthDate, flags)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeString(fathersName)
        parcel.writeString(identityType)
        parcel.writeString(identityImage)
        parcel.writeString(profileImage)
        parcel.writeString(householdId)
        parcel.writeString(identityId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Member> {
        override fun createFromParcel(parcel: Parcel): Member {
            return Member(parcel)
        }

        override fun newArray(size: Int): Array<Member?> {
            return arrayOfNulls(size)
        }
    }


}
