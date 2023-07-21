package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Date(
    @Expose @field:SerializedName("day")
    @ColumnInfo(name = "day")
    var day: Int,
    @Expose @field:SerializedName("year")
    @ColumnInfo(name = "year")
    var year: Int,
    @Expose @field:SerializedName("month")
    @ColumnInfo(name = "month")
    var month: Int
) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(day)
        parcel.writeInt(year)
        parcel.writeInt(month)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Date> {
        override fun createFromParcel(parcel: Parcel): Date {
            return Date(parcel)
        }

        override fun newArray(size: Int): Array<Date?> {
            return arrayOfNulls(size)
        }
    }

}
