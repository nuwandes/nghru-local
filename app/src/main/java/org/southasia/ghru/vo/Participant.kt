package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


@Entity(tableName = "participant")
data class Participant(
    @Expose @field:SerializedName("id")
    @ColumnInfo(name = "id")
    var participantId: String,
    @Expose @field:SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String,
    @Expose @field:SerializedName("age")
    @ColumnInfo(name = "age")
    var age: Int,
    @Expose @field:SerializedName("gender")
    @ColumnInfo(name = "gender")
    val gender: String,
    @Expose @field:SerializedName("father_name")
    @ColumnInfo(name = "father_name")
    var fatherName: String,
    @Expose @field:SerializedName("mother_name")
    @ColumnInfo(name = "mother_name")
    val motherName: String,
    @Expose @field:SerializedName("id_number")
    @ColumnInfo(name = "id_number")
    val idNumber: String,
    @Expose @field:SerializedName("screening_id")
    @ColumnInfo(name = "screening_id")
    var screeningId: String,
    @Expose @field:SerializedName("profile_url")
    @ColumnInfo(name = "profile_url")
    var profileUrl: String,
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false
    //@ColumnInfo(name = "sync_pending")
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "particaipant_id")
    @Expose
    @field:SerializedName("particaipant_id")
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()
    @field:SerializedName("stations")
    @Ignore
    var stations: List<Station> = emptyList()

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        stations = parcel.createTypedArrayList(Station)!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(participantId)
        parcel.writeString(name)
        parcel.writeInt(age)
        parcel.writeString(gender)
        parcel.writeString(fatherName)
        parcel.writeString(motherName)
        parcel.writeString(idNumber)
        parcel.writeString(screeningId)
        parcel.writeString(profileUrl)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeTypedList(stations)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Participant> {
        override fun createFromParcel(parcel: Parcel): Participant {
            return Participant(parcel)
        }

        override fun newArray(size: Int): Array<Participant?> {
            return arrayOfNulls(size)
        }
    }

}
