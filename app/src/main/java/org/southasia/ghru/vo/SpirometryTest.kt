package org.southasia.ghru.vo

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.nuvoair.sdk.launcher.NuvoairLauncherMeasurement
import java.io.Serializable

@Entity(tableName = "spirometry_request")
data class SpirometryRequest(
    @Expose @field:SerializedName("data") var data: String,
    @Expose @field:SerializedName("comment") var comment: String?,
    @Embedded(prefix = "meta") @Expose @field:SerializedName("meta") var meta: Meta?
) :
    Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readParcelable(Meta::class.java.classLoader)
    ) {
    }


    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    @ColumnInfo(name = "timestamp")
    var timestamp: Long = System.currentTimeMillis()

    @ColumnInfo(name = "sync_pending")
    var syncPending: Boolean = false

    @ColumnInfo(name = "screening_id")
    lateinit var screeningId: String

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(data)
        parcel.writeString(comment)
        parcel.writeParcelable(meta, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpirometryRequest> {
        override fun createFromParcel(parcel: Parcel): SpirometryRequest {
            return SpirometryRequest(parcel)
        }

        override fun newArray(size: Int): Array<SpirometryRequest?> {
            return arrayOfNulls(size)
        }
    }

}

data class SpirometryData(@Expose @field:SerializedName("body") var body: SpirometryTests)
data class SpirometryTests(
    @Expose @field:SerializedName("tests") var tests: List<SpirometryTest>,
    @Expose @field:SerializedName("device_id") var device_id: String?,
    @Expose @field:SerializedName("turbine_id") var turbine_id: String?,
    @Expose @field:SerializedName("device_data") var deviceData: NuvoairLauncherMeasurement?

)

data class SpirometryTest(
    @Expose @field:SerializedName("test_number") var testNumber: Int,
    @Expose @field:SerializedName("fev_actual") var fev: String,
    @Expose @field:SerializedName("fvc_actual") var fvc: String,
    @Expose @field:SerializedName("fev_fvc_ratio") var ratio: String,
    @Expose @field:SerializedName("PEFR") var pev: String

)