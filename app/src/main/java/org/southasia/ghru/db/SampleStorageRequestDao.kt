package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.SampleStorageRequest

/**
 * Interface for database access for User related operations.
 */
@Dao
interface SampleStorageRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sampleStorageRequest: SampleStorageRequest): Long

    @Update
    fun update(sampleStorageRequest: SampleStorageRequest): Int

    @Delete
    fun delete(sampleStorageRequest: SampleStorageRequest)

    @Query("SELECT * FROM sample_storage_request WHERE id = :id")
    fun getSampleStorageRequest(id: Long): LiveData<SampleStorageRequest>

    @Query("SELECT * FROM sample_storage_request")
    fun getSampleStorageRequests(): LiveData<List<SampleStorageRequest>>

}
