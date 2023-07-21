package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Sample(
    @Expose @field:SerializedName("sample_id") var sampleId: String?,
    @Expose @field:SerializedName("status") var status: String?,
    @Expose @field:SerializedName("created_at") var createdAt: String?,
    @Expose @field:SerializedName("updated_at") var updatedAt: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sampleId)
        parcel.writeString(status)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sample> {
        override fun createFromParcel(parcel: Parcel): Sample {
            return Sample(parcel)
        }

        override fun newArray(size: Int): Array<Sample?> {
            return arrayOfNulls(size)
        }
    }

}
