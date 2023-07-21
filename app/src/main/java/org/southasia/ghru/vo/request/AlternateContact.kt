package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AlternateContact(
    @Expose @SerializedName("name") val name: String?,
    @Expose @SerializedName("relationship") val relationship: String?,
    @Expose @SerializedName("address") val address: String?,
    @Expose @SerializedName("phone_preferred") val phonePreferred: String?,
    @Expose @SerializedName("phone_alternate") val phoneAlternate: String?

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(relationship)
        parcel.writeString(address)
        parcel.writeString(phonePreferred)
        parcel.writeString(phoneAlternate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AlternateContact> {
        override fun createFromParcel(parcel: Parcel): AlternateContact {
            return AlternateContact(parcel)
        }

        override fun newArray(size: Int): Array<AlternateContact?> {
            return arrayOfNulls(size)
        }
    }


}