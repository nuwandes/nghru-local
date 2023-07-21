package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.BodyMeasurementRequest

/**
 * Interface for database access for User related operations.
 */
@Dao
interface BodyMeasurementRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bodyMeasurementRequest: BodyMeasurementRequest): Long

    @Insert
    fun insertAll(users: List<BodyMeasurementRequest>)

    @Update
    fun update(bodyMeasurementRequest: BodyMeasurementRequest): Int

    @Delete
    fun delete(bodyMeasurementRequest: BodyMeasurementRequest)


    @Query("SELECT * FROM body_measurement_request WHERE id = :id")
    fun getBodyMeasurementRequest(id: Long): LiveData<BodyMeasurementRequest>

    @Query("SELECT * FROM body_measurement_request")
    fun getBodyMeasurementRequests(): LiveData<List<BodyMeasurementRequest>>

}
