package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Sampleprocessdata(
    @Expose @field:SerializedName("hbA1c") val hb1Ac: Hb1AcDto?,
    @Expose @field:SerializedName("fasting_blood_glucose") val fastingBloodGlucose: FastingBloodGlucoseDto?,
    @Expose @field:SerializedName("lipid_profile") val lipidProfile: LipidProfileAllDto?,
    @Expose @field:SerializedName("hogtt") val hOGTT: HOGTTDto?,
    @Expose @field:SerializedName("hemoglobin") val hemog: HemoglobinDto?

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Hb1AcDto::class.java.classLoader),
        parcel.readParcelable(FastingBloodGlucoseDto::class.java.classLoader),
        parcel.readParcelable(LipidProfileAllDto::class.java.classLoader),
        parcel.readParcelable(HOGTTDto::class.java.classLoader),
        parcel.readParcelable(HemoglobinDto::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(hb1Ac, flags)
        parcel.writeParcelable(fastingBloodGlucose, flags)
        parcel.writeParcelable(lipidProfile, flags)
        parcel.writeParcelable(hOGTT, flags)
        parcel.writeParcelable(hemog, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sampleprocessdata> {
        override fun createFromParcel(parcel: Parcel): Sampleprocessdata {
            return Sampleprocessdata(parcel)
        }

        override fun newArray(size: Int): Array<Sampleprocessdata?> {
            return arrayOfNulls(size)
        }
    }
}
