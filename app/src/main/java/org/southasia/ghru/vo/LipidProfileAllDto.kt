package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class LipidProfileAllDto(
    @Expose @field:SerializedName("total_cholesterol")
    var totalCholesterol: TotalCholesterolDto?,
    @Expose @field:SerializedName("hdl")
    var hdl: HDLDto?,
    @Expose @field:SerializedName("triglycerol")
    var triglycerol: TriglyceridesDto?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(TotalCholesterolDto::class.java.classLoader),
        parcel.readParcelable(HDLDto::class.java.classLoader),
        parcel.readParcelable(TriglyceridesDto::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(totalCholesterol, flags)
        parcel.writeParcelable(hdl, flags)
        parcel.writeParcelable(triglycerol, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LipidProfileAllDto> {
        override fun createFromParcel(parcel: Parcel): LipidProfileAllDto {
            return LipidProfileAllDto(parcel)
        }

        override fun newArray(size: Int): Array<LipidProfileAllDto?> {
            return arrayOfNulls(size)
        }
    }


}