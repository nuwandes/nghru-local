package org.southasia.ghru.vo.request

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.vo.Comment
import org.southasia.ghru.vo.Meta
import java.io.Serializable

@Entity(tableName = "sample_request")
data class SampleRequest(
    @Expose @SerializedName("screening_id") @ColumnInfo(name = "screening_id") val screeningId: String?,
    @PrimaryKey @Expose @SerializedName("sample_id") @ColumnInfo(name = "sample_id") val sampleId: String,
    @Expose @SerializedName("comment") @Embedded(prefix = "comment") val comment: Comment?,
    @Expose @SerializedName("is_cancelled") @ColumnInfo(name = "is_cancelled") var isCancelled: Int? = 0
) : Serializable, Parcelable {


    var timestamp: Long = System.currentTimeMillis()
    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @Expose
    @SerializedName("collected_by")
    @ColumnInfo(name = "collected_by")
    var collectedBy: String? = null

    @Expose
    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    var createdAt: String? = null


    @Expose
    @SerializedName("storage_id")
    @ColumnInfo(name = "storage_id")
    var storageId: String? = null


    @ColumnInfo(name = "freezer_id")
    var freezerId: String? = null

    @Expose
    @SerializedName("status_code")
    @ColumnInfo(name = "status_code")
    var statusCode: Int? = null

    @Expose
    @SerializedName("meta")
    @Ignore
    @Embedded(prefix = "meta")
    var meta: Meta? = null

    @ColumnInfo(name = "meta_id")
    var metaId: Long = 0


    constructor(parcel: Parcel) : this(

        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(Comment::class.java.classLoader)
    ) {

        timestamp = parcel.readLong()
        syncPending = parcel.readByte() != 0.toByte()
        collectedBy = parcel.readString()
        createdAt = parcel.readString()
        storageId = parcel.readString()
        freezerId = parcel.readString()
        statusCode = parcel.readValue(Int::class.java.classLoader) as? Int
        meta = parcel.readParcelable(Meta::class.java.classLoader)
        isCancelled = parcel.readValue(Int::class.java.classLoader) as? Int
        metaId = parcel.readLong()
//        id = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(screeningId)
        parcel.writeString(sampleId)
        parcel.writeParcelable(comment, flags)
        parcel.writeLong(timestamp)
        parcel.writeByte(if (syncPending) 1 else 0)
        parcel.writeString(collectedBy)
        parcel.writeString(createdAt)
        parcel.writeString(storageId)
        parcel.writeString(freezerId)
        parcel.writeValue(statusCode)
        parcel.writeParcelable(meta, flags)
        parcel.writeValue(isCancelled)
        parcel.writeLong(metaId)
//        parcel.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SampleRequest> {
        override fun createFromParcel(parcel: Parcel): SampleRequest {
            return SampleRequest(parcel)
        }

        override fun newArray(size: Int): Array<SampleRequest?> {
            return arrayOfNulls(size)
        }
    }


}