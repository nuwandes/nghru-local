package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable

class DateOfBirth(var day: String, var month: String, var year: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(day)
        parcel.writeString(month)
        parcel.writeString(year)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DateOfBirth> {
        override fun createFromParcel(parcel: Parcel): DateOfBirth {
            return DateOfBirth(parcel)
        }

        override fun newArray(size: Int): Array<DateOfBirth?> {
            return arrayOfNulls(size)
        }
    }
}