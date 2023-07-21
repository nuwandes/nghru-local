package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


/**
 * Created by shanuka on 10/26/17.
 */
@Entity(
    tableName = "fundoscopy_request"
)
data class FundoscopyRequest(
    @Expose @SerializedName(value = "comment")  @ColumnInfo(name = "comment") var comment: String? = null,
    @Expose @SerializedName(value = "device_id") @ColumnInfo(name = "device_id") var device_id: String? = null,
    @Expose @SerializedName(value = "pupil_dilation") @ColumnInfo(name = "pupil_dilation")  var pupil_dilation: Boolean? = null,
    @Expose @field:SerializedName("meta")  @Embedded(prefix = "meta") var meta: Meta?,
    @Expose @SerializedName(value = "cataract_observation") @ColumnInfo(name = "cataract_observation") var cataract_observation: String? = null
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @ColumnInfo(name = "screening_id")
    lateinit var screeningId: String

    @ColumnInfo(name = "fundoscopy_meta_id")
    var fundoscopyMetaId: Long = 0


    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readParcelable(Meta::class.java.classLoader),
        parcel.readString()
    ) {
        id = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        screeningId = parcel.readString()
        fundoscopyMetaId= parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comment)
        parcel.writeString(device_id)
        parcel.writeByte(if (pupil_dilation!!) 1 else 0)
        parcel.writeParcelable(meta, flags)
        parcel.writeLong(id)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeString(screeningId)
        parcel.writeLong(fundoscopyMetaId)
        parcel.writeString(cataract_observation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FundoscopyRequest> {
        override fun createFromParcel(parcel: Parcel): FundoscopyRequest {
            return FundoscopyRequest(parcel)
        }

        override fun newArray(size: Int): Array<FundoscopyRequest?> {
            return arrayOfNulls(size)
        }
    }


}
