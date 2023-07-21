package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    indices = [
        (Index("id"))], tableName = "identification"
)
data class Identification(
    @Expose @field:SerializedName("type")
    @ColumnInfo(name = "type")
    var type: String,
    @Expose @field:SerializedName("value")
    @ColumnInfo(name = "value")
    val value: String
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    ) {
        id = parcel.readLong()
    }

    @Ignore
    constructor() : this(
        "",
        ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(value)
        parcel.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Identification> {
        override fun createFromParcel(parcel: Parcel): Identification {
            return Identification(parcel)
        }

        override fun newArray(size: Int): Array<Identification?> {
            return arrayOfNulls(size)
        }
    }
}