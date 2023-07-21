package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Meta
import java.io.Serializable

@Entity(tableName = "participant_request")
data class ParticipantRequest(
    @Expose @SerializedName("first_name") @ColumnInfo(name = "first_name") val firstName: String,
    @Expose @SerializedName("last_name") @ColumnInfo(name = "last_name") val lastName: String,
    @Expose @SerializedName("age") @Embedded(prefix = "age_") val age: ParticipantAge,
    @Expose @SerializedName("gender") @ColumnInfo(name = "gender") val gender: String,
    @Expose @SerializedName("id_number") @ColumnInfo(name = "id_number") val idNumber: String,
    @Expose @SerializedName("father_name") @ColumnInfo(name = "father_name") val fatherName: String,
    @Expose @SerializedName("id_type") @ColumnInfo(name = "id_type") val idType: String,
    @Expose @SerializedName("screening_id") @ColumnInfo(name = "screening_id") val screeningId: String,
    @Expose @SerializedName("household_id") @ColumnInfo(name = "household_id") val householdId: String?,
    @Expose @SerializedName("member_id") @ColumnInfo(name = "member_id") val memberId: String?,
    @SerializedName("identity_image") @ColumnInfo(name = "identity_mage") val identityImage: String?,
    @Expose(
        deserialize = true,
        serialize = false
    ) @SerializedName("profile_url") @ColumnInfo(name = "profile_image") val profileImage: String?,
    @Expose @SerializedName("contact_number") @ColumnInfo(name = "contact_number") val contactNumber: String,
    @Expose @SerializedName("comment") @ColumnInfo(name = "comment") val comment: String?
) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var timestamp: Long = System.currentTimeMillis()

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false
    @Ignore
    var meta: Meta? = null

    @ColumnInfo(name = "created_date_time")
    var createdDateTime: String = ""

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(ParticipantAge::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        createdDateTime = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeParcelable(age, flags)
        parcel.writeString(gender)
        parcel.writeString(idNumber)
        parcel.writeString(fatherName)
        parcel.writeString(idType)
        parcel.writeString(screeningId)
        parcel.writeString(householdId)
        parcel.writeString(memberId)
        parcel.writeString(identityImage)
        parcel.writeString(profileImage)
        parcel.writeString(contactNumber)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeString(comment)
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




