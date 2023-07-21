package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Ignore
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class StorageDto(@Expose @field:SerializedName("freezer_id") var freezerId: String?) : Serializable, Parcelable {

    @Expose
    @SerializedName("meta")
    @Ignore
    var meta: Meta? = null

    constructor(parcel: Parcel) : this(parcel.readString()) {
        meta = parcel.readParcelable(Meta::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(freezerId)
        parcel.writeParcelable(meta, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StorageDto> {
        override fun createFromParcel(parcel: Parcel): StorageDto {
            return StorageDto(parcel)
        }

        override fun newArray(size: Int): Array<StorageDto?> {
            return arrayOfNulls(size)
        }
    }


}