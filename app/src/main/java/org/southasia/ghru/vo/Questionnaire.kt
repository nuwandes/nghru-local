package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "questionnaire")
data class Questionnaire(
     @Expose @field:SerializedName("id")
    @ColumnInfo(name = "id")
    var id: String,
    @Expose @field:SerializedName("name")
    @ColumnInfo(name = "name")
    var name: String?,
    @Expose @field:SerializedName("json")
    @ColumnInfo(name = "json")
    var json: String?,
    @Expose @field:SerializedName("language")
    @ColumnInfo(name = "language")
    val language: String?,
    @Expose @field:SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    val created_at: String?,
     @Expose @field:SerializedName("language_full")
     @ColumnInfo(name = "language_full")
     val languageFull: String?
) : Serializable, Parcelable {

    @PrimaryKey (autoGenerate = true)
    var new_id :Int = 0

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
        new_id = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(json)
        parcel.writeString(language)
        parcel.writeString(created_at)
        parcel.writeString(languageFull)
        parcel.writeInt(new_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Questionnaire> {
        override fun createFromParcel(parcel: Parcel): Questionnaire {
            return Questionnaire(parcel)
        }

        override fun newArray(size: Int): Array<Questionnaire?> {
            return arrayOfNulls(size)
        }
    }


}
