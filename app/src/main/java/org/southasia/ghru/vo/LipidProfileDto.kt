package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class LipidProfileDto(
    @Expose @field:SerializedName("lot_id")
    var lot_id: String,
    @Expose @field:SerializedName("total_cholesterol")
    var totalCholesterol: String,
    @Expose @field:SerializedName("lDLC")
    var lDLC: String, var hDL: String,
    @Expose @field:SerializedName("triglycerol")
    var triglycerol: String
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(lot_id)
        parcel.writeString(totalCholesterol)
        parcel.writeString(lDLC)
        parcel.writeString(hDL)
        parcel.writeString(triglycerol)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LipidProfileDto> {
        override fun createFromParcel(parcel: Parcel): LipidProfileDto {
            return LipidProfileDto(parcel)
        }

        override fun newArray(size: Int): Array<LipidProfileDto?> {
            return arrayOfNulls(size)
        }
    }

}