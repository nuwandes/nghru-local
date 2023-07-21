package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Comment
import org.southasia.ghru.vo.Meta
import java.io.Serializable


data class SampleCreateRequest(
    @Expose @SerializedName("meta")  val meta: Meta?,
    @Expose @SerializedName("comment")  val comment: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Meta::class.java.classLoader),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(meta, flags)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SampleCreateRequest> {
        override fun createFromParcel(parcel: Parcel): SampleCreateRequest {
            return SampleCreateRequest(parcel)
        }

        override fun newArray(size: Int): Array<SampleCreateRequest?> {
            return arrayOfNulls(size)
        }
    }


}