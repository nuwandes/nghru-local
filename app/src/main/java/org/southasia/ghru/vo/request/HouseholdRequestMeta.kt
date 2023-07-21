package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.db.MemberTypeConverters
import org.southasia.ghru.vo.Meta
import java.io.Serializable

@Entity(tableName = "household_request_meta")
@TypeConverters(MemberTypeConverters::class)
data class HouseholdRequestMeta(
    @Expose @SerializedName("meta") @Embedded(prefix = "meta_") val meta: Meta?,
    @Expose @SerializedName("body") @Embedded(prefix = "body_") val householdRequest: HouseholdRequest?,
    @Expose @SerializedName("uuid") val uuid: String?

) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var timestamp: Long = System.currentTimeMillis()

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    constructor(meta: Meta?, householdRequest: HouseholdRequest?) : this(
        meta = meta,
        householdRequest = householdRequest,
        uuid = null
    )

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Meta::class.java.classLoader),
        parcel.readParcelable(HouseholdRequest::class.java.classLoader),
        parcel.readString()
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(meta, flags)
        parcel.writeParcelable(householdRequest, flags)
        parcel.writeString(uuid)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HouseholdRequestMeta> {
        override fun createFromParcel(parcel: Parcel): HouseholdRequestMeta {
            return HouseholdRequestMeta(parcel)
        }

        override fun newArray(size: Int): Array<HouseholdRequestMeta?> {
            return arrayOfNulls(size)
        }
    }


}
