package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "household_request", indices = [
        (Index("enumeration_id", unique = true))]
)
data class HouseholdRequest(
    @Expose @SerializedName("address") @Embedded(prefix = "household_request_") val address: Address,
    @Expose @SerializedName("location") @Embedded(prefix = "position_") val position: Position?

) : Serializable, Parcelable {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var timestamp: Long = System.currentTimeMillis()
    @Expose
    @SerializedName("revisit")
    @ColumnInfo(name = "revisit")
    var revisit: Boolean = false
    @Expose
    @SerializedName("enumeration_id")
    @ColumnInfo(name = "enumeration_id")
    var enumerationId: String = ""
    @Ignore
    var memberList: List<Member>? = null
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @Expose
    @SerializedName("consent")
    @Embedded(prefix = "consent_")
    var consent: Consent? = null

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Address::class.java.classLoader)!!,
        parcel.readParcelable(Position::class.java.classLoader)
    ) {
        id = parcel.readLong()
        timestamp = parcel.readLong()
        revisit = parcel.readByte() != 0.toByte()
        enumerationId = parcel.readString()!!
        memberList = parcel.createTypedArrayList(Member)
        syncPending = parcel.readByte() != 0.toByte()
        consent = parcel.readParcelable(Consent::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(address, flags)
        parcel.writeParcelable(position, flags)
        parcel.writeLong(id)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (revisit) 1 else 0)
        parcel.writeString(enumerationId)
        parcel.writeTypedList(memberList)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeParcelable(consent, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HouseholdRequest> {
        override fun createFromParcel(parcel: Parcel): HouseholdRequest {
            return HouseholdRequest(parcel)
        }

        override fun newArray(size: Int): Array<HouseholdRequest?> {
            return arrayOfNulls(size)
        }
    }

}


data class Consent(
    @Expose @field:SerializedName("status")
    @ColumnInfo(name = "status")
    var status: Boolean,
    @Expose @field:SerializedName("reason")
    @ColumnInfo(name = "reason")
    val reason: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (status) 1 else 0)
        parcel.writeString(reason)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Consent> {
        override fun createFromParcel(parcel: Parcel): Consent {
            return Consent(parcel)
        }

        override fun newArray(size: Int): Array<Consent?> {
            return arrayOfNulls(size)
        }
    }

}
