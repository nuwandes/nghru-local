package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.ECGStatus
import org.southasia.ghru.vo.request.CancelRequest


/**
 * Interface for database access for User related operations.
 */
@Dao
interface CancelRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: CancelRequest): Long

    @Query("UPDATE cancel_request SET sync_pending = 0 WHERE sync_pending = 1 AND screening_id = :screeningId")
    fun update(screeningId: String): Int

    @Query("SELECT * FROM cancel_request WHERE id =:id")
    fun findById(id: String): LiveData<CancelRequest>

    @Query("DELETE FROM cancel_request WHERE id = :id")
    fun deleteRequest(id : Long)

    @Query("SELECT * FROM cancel_request LIMIT 1")
    fun getCancelRequest(): LiveData<CancelRequest>

    @Query("SELECT * FROM cancel_request WHERE sync_pending = 1 ORDER BY id ASC")
    fun getCancelRequestSyncPending(): LiveData<List<CancelRequest>>
}
