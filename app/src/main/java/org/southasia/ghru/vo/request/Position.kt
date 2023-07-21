package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "position")
data class Position(
    @Expose @field:SerializedName("longitude")
    @ColumnInfo(name = "longitude")
    var longitude: Double?,
    @Expose @field:SerializedName("latitude")
    @ColumnInfo(name = "latitude")
    val latitude: Double?,
    @Expose @field:SerializedName("accuracy_radius")
    @ColumnInfo(name = "accuracy_radius")
    val accuracyRadius: Int?,
    @Expose @field:SerializedName("identifier")
    @ColumnInfo(name = "identifier")
    val identifier: String?
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    constructor(parcel: Parcel) : this(
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
    }


    @Ignore
    constructor() : this(
        null,
        null, null, null
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(longitude)
        parcel.writeValue(latitude)
        parcel.writeValue(accuracyRadius)
        parcel.writeString(identifier)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Position> {
        override fun createFromParcel(parcel: Parcel): Position {
            return Position(parcel)
        }

        override fun newArray(size: Int): Array<Position?> {
            return arrayOfNulls(size)
        }
    }


}