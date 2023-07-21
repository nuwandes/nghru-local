package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.FundoscopyRequest


@Dao
interface FundoscopyRequestDao {

    @Query("DELETE FROM fundoscopy_request")
    fun nukeTable(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fundoscopyRequest: FundoscopyRequest): Long

    @Query("UPDATE fundoscopy_request SET sync_pending = 0 WHERE sync_pending = 1 AND screening_id = :screeningId")
    fun update(screeningId: String): Int

    @Delete
    fun delete(fundoscopyRequest: FundoscopyRequest)

    @Query("DELETE FROM fundoscopy_request WHERE id = :id")
    fun deleteRequest(id : Long)

    @Query("SELECT * FROM fundoscopy_request WHERE id = :id")
    fun getECGStatus(id: Long): LiveData<FundoscopyRequest>

    @Query("SELECT * FROM fundoscopy_request")
    fun getECGStatuses(): LiveData<List<FundoscopyRequest>>

    @Query("SELECT * FROM fundoscopy_request WHERE sync_pending = 1 ORDER BY id ASC")
    fun getFundoscopyRequestSyncPending(): LiveData<List<FundoscopyRequest>>
}