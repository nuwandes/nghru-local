package org.southasia.ghru.vo.request


import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Meta
import java.io.Serializable


data class HouseholdRequestMetaResponce(
    @Expose @SerializedName("meta") val meta: Meta?,
    @Expose @SerializedName("body") val householdRequest: HouseholdRequest?,
    @Expose @SerializedName("members") var memberList: ArrayList<Member>? = null,
    @Expose @SerializedName("uuid") val uuid: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Meta::class.java.classLoader),
        parcel.readParcelable(HouseholdRequest::class.java.classLoader),
        parcel.createTypedArrayList(Member),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(meta, flags)
        parcel.writeParcelable(householdRequest, flags)
        parcel.writeTypedList(memberList)
        parcel.writeString(uuid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HouseholdRequestMetaResponce> {
        override fun createFromParcel(parcel: Parcel): HouseholdRequestMetaResponce {
            return HouseholdRequestMetaResponce(parcel)
        }

        override fun newArray(size: Int): Array<HouseholdRequestMetaResponce?> {
            return arrayOfNulls(size)
        }
    }

}
