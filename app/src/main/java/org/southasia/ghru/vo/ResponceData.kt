package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
class ResponceData(
    @Expose @field:SerializedName(value = "error")
    val error: Boolean?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(parcel.readValue(Boolean::class.java.classLoader) as? Boolean) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(error)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ResponceData> {
        override fun createFromParcel(parcel: Parcel): ResponceData {
            return ResponceData(parcel)
        }

        override fun newArray(size: Int): Array<ResponceData?> {
            return arrayOfNulls(size)
        }
    }

}
