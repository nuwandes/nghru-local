package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.SpirometryRequest


/**
 * Interface for database access for User related operations.
 */
@Dao
interface SpiromentryRequestDao {


    @Query("DELETE FROM spirometry_request")
    fun nukeTable(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(spirometryRequest: SpirometryRequest): Long

    @Query("UPDATE spirometry_request SET sync_pending = 0 WHERE sync_pending = 1 AND screening_id = :screeningId")
    fun update(screeningId: String): Int

    @Delete
    fun delete(spirometryRequest: SpirometryRequest)

    @Query("DELETE FROM spirometry_request WHERE id = :id")
    fun deleteRequest(id : Long)

    @Query("SELECT * FROM spirometry_request WHERE id = :id")
    fun getSpirometryRequest(id: Long): LiveData<SpirometryRequest>

    @Query("SELECT * FROM spirometry_request")
    fun getSpirometryRequests(): LiveData<List<SpirometryRequest>>

    @Query("SELECT * FROM spirometry_request WHERE screening_id=:screeningId ORDER BY id DESC LIMIT 1")
    fun getSpirometryRequests(screeningId: String): LiveData<SpirometryRequest>

    //SELECT * FROM access_token LIMIT 1

    @Query("SELECT * FROM spirometry_request WHERE sync_pending = 1 ORDER BY id ASC")
    fun getSpirometryRequestSyncPending():  LiveData<List<SpirometryRequest>>

}
