package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    indices = [
        (Index("household_id"))], tableName = "household"
)
data class Household(
    @Expose @field:SerializedName("household_id")
    @ColumnInfo(name = "household_id")
    var householdId: String,
    @Expose @field:SerializedName("location_code")
    @ColumnInfo(name = "location_code")
    val locationCode: String,
    @Expose @field:SerializedName("location")
    @ColumnInfo(name = "location")
    val location: String,
    @Expose @field:SerializedName("position")
    @field:Embedded(prefix = "position_")
    val latLong: Position,
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readParcelable(Position::class.java.classLoader)!!,
        parcel.readByte() != 0.toByte()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
    }

    @Ignore
    constructor() : this(
        "",
        "",
        "",
        Position(),
        false
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(householdId)
        parcel.writeString(locationCode)
        parcel.writeString(location)
        parcel.writeParcelable(latLong, flags)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Household> {
        override fun createFromParcel(parcel: Parcel): Household {
            return Household(parcel)
        }

        override fun newArray(size: Int): Array<Household?> {
            return arrayOfNulls(size)
        }
    }


}