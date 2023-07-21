package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.ParticipantRequest

/**
 * Interface for database access for User related operations.
 */
@Dao
interface ParticipantRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(householdRequest: ParticipantRequest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(householdRequest: List<ParticipantRequest>)

    @Update
    fun update(householdRequest: ParticipantRequest): Int


    @Delete
    fun delete(householdRequest: ParticipantRequest)

//    @Query("SELECT * FROM participan_request WHERE identifier = :identifier ORDER BY timestamp DESC")
//    fun getParticipantRequests(identifier: String): LiveData<ParticipantRequest>


    @Query("UPDATE participant_request SET sync_pending = 0")
    fun updateSyncPendingHouseholdRequest()

    @Query("SELECT * FROM participant_request WHERE id = :id")
    fun getParticipantRequest(id: Long): LiveData<ParticipantRequest>

    @Query("SELECT * FROM participant_request")
    fun getParticipantRequests(): LiveData<List<ParticipantRequest>>

    @Query("SELECT * FROM participant_request WHERE screening_id = :screeningId")
    fun getParticipantRequestByScreenId(screeningId: String): LiveData<ParticipantRequest>

    @Query("SELECT * FROM participant_request WHERE id_number = :idNumber")
    fun getParticipantByIdnumber(idNumber: String): LiveData<ParticipantRequest>

    @Query("SELECT * FROM participant_request WHERE sync_pending = :syncStatus")
    fun getParticipantRequestsBySyncStatus(syncStatus : Boolean): LiveData<List<ParticipantRequest>>



}
