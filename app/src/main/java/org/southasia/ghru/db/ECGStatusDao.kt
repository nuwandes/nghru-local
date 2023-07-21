package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.ECGStatus
import org.southasia.ghru.vo.request.BodyMeasurementMeta

@Dao
interface ECGStatusDao {

    @Query("DELETE FROM ecg_status")
    fun nukeTable(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ecgStatus: ECGStatus): Long


    @Query("UPDATE ecg_status SET sync_pending = 0 WHERE sync_pending = 1")
    fun update()

    @Delete
    fun delete(ecgStatus: ECGStatus)

    @Query("DELETE FROM ecg_status WHERE id = :id")
    fun deleteRequest(id : Long)

    @Query("SELECT * FROM ecg_status WHERE id = :id")
    fun getECGStatus(id: Long): LiveData<ECGStatus>

    @Query("SELECT * FROM ecg_status")
    fun getECGStatuses(): LiveData<List<ECGStatus>>

    @Query("SELECT * FROM ecg_status WHERE sync_pending = 1 ORDER BY id ASC")
    fun getECGStatusesSyncPending():LiveData<List<ECGStatus>>


}