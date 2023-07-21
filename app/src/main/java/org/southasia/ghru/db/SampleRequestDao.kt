package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.SampleRequest

/**
 * Interface for database access for User related operations.
 */
@Dao
interface SampleRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sampleRequest: SampleRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(contributors: List<SampleRequest>)

    @Update
    fun update(sampleRequest: SampleRequest): Int

    @Query("UPDATE sample_request SET sync_pending = 0 WHERE sync_pending = 1 AND screening_id = :screeningId")
    fun update(screeningId: String): Int

    @Delete
    fun delete(sampleRequest: SampleRequest)

//    @Query("SELECT * FROM participan_request WHERE identifier = :identifier ORDER BY timestamp DESC")
//    fun getSampleRequests(identifier: String): LiveData<SampleRequest>

    @Query("SELECT * FROM sample_request WHERE sample_id = :sampleId")
    fun getSampleRequest(sampleId: String): LiveData<SampleRequest>

    @Query("SELECT * FROM sample_request WHERE storage_id = :storageId")
    fun getSampleRequestByStorageId(storageId: String): LiveData<SampleRequest>

    @Query("SELECT * FROM sample_request WHERE sample_id = :id")
    fun getSampleRequestByID(id: String): LiveData<SampleRequest>

    @Query("SELECT * FROM sample_request where  status_code = :statusCode AND is_cancelled = :isCancelled")
    fun getSampleRequests(statusCode: Int, isCancelled:Int): LiveData<List<SampleRequest>>

    @Query("SELECT * FROM sample_request where status_code = :statusCode AND is_cancelled = :isCancelled")
    fun getSampleRequestsStorage(statusCode: Int, isCancelled:Int): LiveData<List<SampleRequest>>

    @Query("SELECT * FROM sample_request WHERE sample_id = :sampleId AND is_cancelled = :isCancelled")
    fun getSampleRequestBySampleId(sampleId: String, isCancelled:Int): LiveData<SampleRequest>

    @Query("SELECT * FROM sample_request WHERE storage_id = :storageID AND is_cancelled = :isCancelled")
    fun getSampleRequestByStorageIDOfflineByStorageID(storageID: String, isCancelled:Int): LiveData<SampleRequest>


    @Query("SELECT * FROM sample_request")
    fun getSampleRequestAlls(): LiveData<List<SampleRequest>>


    @Query("DELETE FROM sample_request")
    fun nukeTable()

    @Query("UPDATE sample_request SET status_code = :statusCode,  storage_id =:storageId  WHERE sample_id = :sampleId")
    fun updattSampleRequestBySampleId(statusCode: Int, sampleId: String, storageId:String): Int

    @Query("UPDATE sample_request SET status_code = :statusCode WHERE sample_id = :storageID")
    fun updattSampleRequestByStorageID(statusCode: Int, storageID: String): Int

    @Query("SELECT * FROM sample_request WHERE sync_pending = 1 ")
    fun getSampleRequestSyncPending(): List<SampleRequest>

    @Query("DELETE FROM sample_request WHERE sample_id = :id")
    fun deleteRequest(id : String)

}
