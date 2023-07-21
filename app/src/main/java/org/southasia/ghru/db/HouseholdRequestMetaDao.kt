package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.HouseholdRequestMeta

/**
 * Interface for database access for User related operations.
 */
@Dao
interface HouseholdRequestMetaMetaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(householdRequest: HouseholdRequestMeta): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(householdRequest: List<HouseholdRequestMeta>)

    @Update
    fun update(householdRequest: HouseholdRequestMeta): Int

    @Delete
    fun delete(householdRequest: HouseholdRequestMeta)

    @Query("UPDATE household_request_meta SET sync_pending = 0")
    fun updateSyncPendingHouseHold()

    @Query("DELETE FROM household_request_meta WHERE sync_pending=:syncPending")
    fun deleteAll(syncPending: Boolean)


    @Query("SELECT * FROM household_request_meta WHERE id = :id")
    fun getHouseholdRequestMeta(id: Long): LiveData<HouseholdRequestMeta>

    @Query("SELECT * FROM household_request_meta")
    fun getHouseholdRequestMetas(): LiveData<List<HouseholdRequestMeta>>

    @Query("SELECT * FROM household_request_meta INNER JOIN member ON household_request_meta.body_enumeration_id == member.householdId WHERE body_enumeration_id LIKE '%' || :search || '%' OR body_household_request_street LIKE '%' || :search || '%' OR name LIKE '%' || :search || '%' OR contact_no LIKE '%' || :search || '%'")
    fun searchHouseholdRequestMetas(search: String): LiveData<List<HouseholdRequestMeta>>


    @Query("SELECT * FROM household_request_meta WHERE body_enumeration_id =:enumerationId")
    fun getHouseholdByEnumerationId(enumerationId: String): LiveData<HouseholdRequestMeta>


    @Query("SELECT * FROM household_request_meta WHERE sync_pending=:syncPending")
    fun getHouseholdBySyncStatus(syncPending: Boolean): LiveData<List<HouseholdRequestMeta>>
}
