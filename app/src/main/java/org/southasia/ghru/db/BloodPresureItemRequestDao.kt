package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.BloodPresureItemRequest

/**
 * Interface for database access for User related operations.
 */
@Dao
interface BloodPresureItemRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bloodPressureRequest: BloodPresureItemRequest): Long

    @Insert
    fun insertAll(users: List<BloodPresureItemRequest>)

    @Update
    fun update(bloodPressureRequest: BloodPresureItemRequest): Int

    @Delete
    fun delete(bloodPressureRequest: BloodPresureItemRequest)

    @Query("DELETE FROM blood_pressure_item_request")
    fun deleteAll()

    @Query("SELECT * FROM blood_pressure_item_request WHERE id = :id")
    fun getBloodPressureItemRequest(id: Long): LiveData<BloodPresureItemRequest>

    @Query("SELECT * FROM blood_pressure_item_request")
    fun getBloodPressureAllRequests(): LiveData<List<BloodPresureItemRequest>>

    @Query("SELECT * FROM blood_pressure_item_request WHERE blood_presure_request_id = :bloodPressureRequestId")
    fun getBloodPressureItemsByBloodPresureRequestID(bloodPressureRequestId: Long): List<BloodPresureItemRequest>


}
