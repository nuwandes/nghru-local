package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class QuestionMeta(
    @Expose @field:SerializedName("meta") var meta: Meta?,
    @Expose @field:SerializedName("json") var json: String?,
    @Expose @field:SerializedName("screening_id") var screeningId: String?,
    @Expose @field:SerializedName("questionnaire_id") var questionnaireId: String?,
    @Expose @field:SerializedName("language") var language: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Meta::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(meta, flags)
        parcel.writeString(json)
        parcel.writeString(screeningId)
        parcel.writeString(questionnaireId)
        parcel.writeString(language)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<QuestionMeta> {
        override fun createFromParcel(parcel: Parcel): QuestionMeta {
            return QuestionMeta(parcel)
        }

        override fun newArray(size: Int): Array<QuestionMeta?> {
            return arrayOfNulls(size)
        }
    }
}

