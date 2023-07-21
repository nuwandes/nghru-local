package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Meta
import java.io.Serializable

@Entity(
    tableName = "participant_meta"
)
data class ParticipantMeta(
    @Expose @field:SerializedName("meta")
    @Embedded(prefix = "meta")
    var meta: Meta, @Expose @field:SerializedName("body")
    @Embedded(prefix = "body")
    var body: ParticipantX
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    @Ignore
    @field:SerializedName("id")
    var uuid: String? = null


    var phoneCountryCode: String? = ""
    @ColumnInfo(name = "country_code")
    var countryCode : String? = ""

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Meta::class.java.classLoader)!!,
        parcel.readParcelable(ParticipantX::class.java.classLoader)!!
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        phoneCountryCode = parcel.readString()
        countryCode = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(meta, flags)
        parcel.writeParcelable(body, flags)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeString(phoneCountryCode)
        parcel.writeString(countryCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantMeta> {
        override fun createFromParcel(parcel: Parcel): ParticipantMeta {
            return ParticipantMeta(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantMeta?> {
            return arrayOfNulls(size)
        }
    }


}

data class ParticipantX(
    @Expose @field:SerializedName("consent_obtained")
    @ColumnInfo(name = "consent_obtained")
    var consentObtained: Boolean,
    @Expose @field:SerializedName("first_name")
    @ColumnInfo(name = "first_name")
    var firstName: String,
    @Expose @field:SerializedName("last_name")
    @ColumnInfo(name = "last_name")
    var lastName: String,
    @Expose @field:SerializedName("preferred_name")
    @ColumnInfo(name = "preferred_name")
    var preferredName: String,
    @Expose @field:SerializedName("gender")
    @ColumnInfo(name = "gender")
    var gender: String,

    @Expose @field:SerializedName("hours_fasted")
    @ColumnInfo(name = "hours_fasted")
    var hoursFasted: String,
    @Expose @field:SerializedName("enumeration_id")
    @ColumnInfo(name = "enumeration_id")
    var enumerationId: String?,

    @Expose @field:SerializedName("member_id")
    @ColumnInfo(name = "member_id")
    var memberId: String?,
    @Expose @field:SerializedName("id_type")
    @ColumnInfo(name = "id_type")
    var idType: String,

    @Expose @field:SerializedName("video_watched")
    @ColumnInfo(name = "video_watched")
    var videoWatched: Boolean,
    @Expose @field:SerializedName("is_eligible")
    @ColumnInfo(name = "is_eligible")
    var isEligible: Boolean,

    @Expose @field:SerializedName("address")
    @Embedded(prefix = "address")
    var address: ParticipantAddress,
    @Expose @field:SerializedName("age")
    @Embedded(prefix = "age")
    var age: ParticipantAge,
    @Expose @field:SerializedName("contact_details")
    @Embedded(prefix = "contact_details")
    var contactDetails: ParticipantContactDetails,
    @Expose @field:SerializedName("alternate_contacts_details")
    @Embedded(prefix = "alternate_contacts_details")
    var alternateContactsDetails: ParticipantAlternateContactsDetails,
    @Expose @field:SerializedName("comment")
    @Embedded(prefix = "comment")
    var comment: String?

) : Serializable, Parcelable {

    @Expose
    @field:SerializedName("id_number")
    @ColumnInfo(name = "id_number")
    var idNumber: String? = String()

    @Expose
    @field:SerializedName("screening_id")
    @ColumnInfo(name = "screening_id")
    var screeningId: String = String()

    @ColumnInfo(name = "identity_image")
    var identityImage: String = String()

    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readParcelable(ParticipantAddress::class.java.classLoader)!!,
        parcel.readParcelable(ParticipantAge::class.java.classLoader)!!,
        parcel.readParcelable(ParticipantContactDetails::class.java.classLoader)!!,
        parcel.readParcelable(ParticipantAlternateContactsDetails::class.java.classLoader)!!,
        parcel.readString()!!
    ) {
        idNumber = parcel.readString()!!
        screeningId = parcel.readString()!!
        identityImage = parcel.readString()!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (consentObtained) 1 else 0)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(preferredName)
        parcel.writeString(gender)
        parcel.writeString(hoursFasted)
        parcel.writeString(enumerationId)
        parcel.writeString(memberId)
        parcel.writeString(idType)
        parcel.writeByte(if (videoWatched) 1 else 0)
        parcel.writeByte(if (isEligible) 1 else 0)
        parcel.writeParcelable(address, flags)
        parcel.writeParcelable(age, flags)
        parcel.writeParcelable(contactDetails, flags)
        parcel.writeParcelable(alternateContactsDetails, flags)
        parcel.writeString(idNumber)
        parcel.writeString(screeningId)
        parcel.writeString(identityImage)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantX> {
        override fun createFromParcel(parcel: Parcel): ParticipantX {
            return ParticipantX(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantX?> {
            return arrayOfNulls(size)
        }
    }


}

data class ParticipantAddress(
    @Expose @field:SerializedName("street")
    @ColumnInfo(name = "street")
    var street: String,
    @Expose @field:SerializedName("country")
    @ColumnInfo(name = "country")
    var country: String,
    @Expose @field:SerializedName("locality")
    @ColumnInfo(name = "locality")
    var locality: String,
    @Expose @field:SerializedName("postcode")
    @ColumnInfo(name = "postcode")
    var postcode: String
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(street)
        parcel.writeString(country)
        parcel.writeString(locality)
        parcel.writeString(postcode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantAddress> {
        override fun createFromParcel(parcel: Parcel): ParticipantAddress {
            return ParticipantAddress(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantAddress?> {
            return arrayOfNulls(size)
        }
    }


}

data class ParticipantAge(
    @Expose @field:SerializedName("dob")
    @ColumnInfo(name = "dob")
    var dob: String,
    @Expose @field:SerializedName("age_in_years")
    @ColumnInfo(name = "age_in_years")
    var ageInYears: String,
    @Expose @field:SerializedName("dob_computed")
    @ColumnInfo(name = "dob_computed")
    var dobComputed: Boolean
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dob)
        parcel.writeString(ageInYears)
        parcel.writeByte(if (dobComputed) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantAge> {
        override fun createFromParcel(parcel: Parcel): ParticipantAge {
            return ParticipantAge(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantAge?> {
            return arrayOfNulls(size)
        }
    }


}


data class ParticipantContactDetails(
    @Expose @field:SerializedName("phone_number_preferred")
    @ColumnInfo(name = "phone_number_preferred")
    var phoneNumberPreferred: String,
    @Expose @field:SerializedName("phone_number_alternate")
    @ColumnInfo(name = "phone_number_alternate")
    var phoneNumberAlternate: String?,
    @Expose @field:SerializedName("email")
    @ColumnInfo(name = "email")
    var email: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phoneNumberPreferred)
        parcel.writeString(phoneNumberAlternate)
        parcel.writeString(email)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantContactDetails> {
        override fun createFromParcel(parcel: Parcel): ParticipantContactDetails {
            return ParticipantContactDetails(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantContactDetails?> {
            return arrayOfNulls(size)
        }
    }


}


data class ParticipantAlternateContactsDetails(
    @Expose @field:SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String,
    @Expose @field:SerializedName("relationship")
    @ColumnInfo(name = "relationship")
    var relationship: String,
    @Expose @field:SerializedName("address")
    @ColumnInfo(name = "address")
    var address: String,
    @Expose @field:SerializedName("phone_preferred")
    @ColumnInfo(name = "phone_preferred")
    var phone_preferred: String,
    @Expose @field:SerializedName("phone_alternate")
    @ColumnInfo(name = "phone_alternate")
    var phone_alternate: String?,
    @Expose @field:SerializedName("email")
    @ColumnInfo(name = "email")
    var email: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(relationship)
        parcel.writeString(address)
        parcel.writeString(phone_preferred)
        parcel.writeString(phone_alternate)
        parcel.writeString(email)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParticipantAlternateContactsDetails> {
        override fun createFromParcel(parcel: Parcel): ParticipantAlternateContactsDetails {
            return ParticipantAlternateContactsDetails(parcel)
        }

        override fun newArray(size: Int): Array<ParticipantAlternateContactsDetails?> {
            return arrayOfNulls(size)
        }
    }

}



