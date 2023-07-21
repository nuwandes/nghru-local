package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.BloodPresureRequest


@Dao
interface BloodPresureRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bloodPresureRequest: BloodPresureRequest): Long

    @Insert
    fun insertAll(users: List<BloodPresureRequest>)

    @Query("UPDATE blood_pressure_request SET sync_pending = 0 WHERE sync_pending = 1 AND screening_id = :screeningId")
    fun update(screeningId: String): Int


    // @Delete
    // fun delete(bloodPresureRequest: BloodPresureRequest)

    @Query("DELETE FROM blood_pressure_request")
    fun deleteAll()

    @Query("DELETE FROM blood_pressure_request WHERE id = :id")
    fun deleteRequest(id : Long)

    @Query("SELECT * FROM blood_pressure_request WHERE id = :id")
    fun getBloodPressureRequest(id: Long): LiveData<BloodPresureRequest>

    @Query("SELECT * FROM blood_pressure_request")
    fun getBloodPressureRequests(): LiveData<List<BloodPresureRequest>>

    @Query("SELECT * FROM blood_pressure_request WHERE sync_pending = 1 ORDER BY id ASC")
    fun getAllBloodPressureRequestsSyncPending(): List<BloodPresureRequest>
}