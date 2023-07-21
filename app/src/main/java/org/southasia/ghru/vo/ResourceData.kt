package org.southasia.ghru.vo

import androidx.room.ColumnInfo
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
class ResourceData<T>(
    @field:SerializedName(value = "data")
    @Expose val data: T?,
    @Expose @field:SerializedName(value = "message")
    val message: String,
    @Expose @field:SerializedName(value = "error")
    val error: Boolean,
    @Expose @SerializedName("station_status")
    @ColumnInfo(name = "station_status")
    val stationStatus: Boolean
)
