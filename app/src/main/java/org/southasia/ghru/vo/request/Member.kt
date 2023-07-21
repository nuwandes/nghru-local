package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Date
import java.io.Serializable

@Entity(
    indices = [
        (Index("name", "householdId"))], tableName = "member"
)
data class Member(
    @Expose @field:SerializedName("given_name")
    @ColumnInfo(name = "name")
    var name: String?,
    @Expose @field:SerializedName("family_name")
    @ColumnInfo(name = "family_name")
    var familyName: String?,
    @Expose @field:SerializedName("preferred_name")
    @ColumnInfo(name = "nick_name")
    var nickName: String?,
    @Expose @field:SerializedName("gender")
    @ColumnInfo(name = "gender")
    var gender: String?,
    @Expose @field:SerializedName("is_primary_contact")
    @ColumnInfo(name = "is_primary_contact")
    var isPrimaryContact: Boolean,
    @Expose @field:SerializedName("contact_number")
    @ColumnInfo(name = "contact_no")
    var contactNo: String?,
    @Expose @field:SerializedName("age")
    @ColumnInfo(name = "age")
    var age: String?,
    @Expose @field:SerializedName("date_of_birth")
    @ColumnInfo(name = "date_of_birth")
    var dateOfBirth: String?,
    @Expose @field:SerializedName("is_stay")
    @ColumnInfo(name = "is_stay")
    val isStay: Boolean?,
    @Expose @field:SerializedName("is_self")
    @ColumnInfo(name = "is_self")
    val isSelf: Boolean?,
    @Expose @field:SerializedName("is_able_screen")
    @ColumnInfo(name = "is_able_screen")
    val isAbleToScreening: Boolean?,
    @Expose @field:SerializedName("reason")
    @ColumnInfo(name = "reason")
    val reason: String?,
    @Expose @field:SerializedName("appointment_date")
    @ColumnInfo(name = "appointment_date")
    val appointment_date: String?,
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    var householdId: String = String()

    var identityType: String? = String()
    var identityImage: String? = String()
    var profileImage: String? = String()
    var identityId: String? = String()
    @Expose
    @field:SerializedName("birth_date")
    @Embedded(prefix = "member_")
    var birthDate: Date? = null

    @Expose
    @field:SerializedName("member_id")
    @ColumnInfo(name = "member_id")
    var memberId: String? = null

    @Expose
    @field:SerializedName("uuid")
    @ColumnInfo(name = "uuid")
    var uuid: String? = null

    @field:SerializedName("registed")
    @ColumnInfo(name = "registed")
    var registed: Boolean? = false

    @field:SerializedName("study_status")
    @Embedded(prefix = "study_status_")
    var studyStatus: StudyStatus? = null


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        householdId = parcel.readString()!!
        identityType = parcel.readString()!!
        identityImage = parcel.readString()!!
        profileImage = parcel.readString()!!
        identityId = parcel.readString()!!
        birthDate = parcel.readParcelable(Date::class.java.classLoader)
        memberId = parcel.readString()
        uuid = parcel.readString()
        registed = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(familyName)
        parcel.writeString(nickName)
        parcel.writeString(gender)
        parcel.writeByte(if (isPrimaryContact) 1 else 0)
        parcel.writeString(contactNo)
        parcel.writeString(age)
        parcel.writeString(dateOfBirth)
        parcel.writeValue(isStay)
        parcel.writeValue(isSelf)
        parcel.writeValue(isAbleToScreening)
        parcel.writeString(reason)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeString(householdId)
        parcel.writeString(identityType)
        parcel.writeString(identityImage)
        parcel.writeString(profileImage)
        parcel.writeString(identityId)
        parcel.writeParcelable(birthDate, flags)
        parcel.writeString(memberId)
        parcel.writeString(uuid)
        parcel.writeValue(registed)
        parcel.writeValue(appointment_date)

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
