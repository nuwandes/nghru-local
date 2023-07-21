package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.request.Member

/**
 * Interface for database access for User related operations.
 */
@Dao
interface MemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(member: Member): Long

    @Insert
    fun insertAll(users: List<Member>)

    @Update
    fun update(member: Member): Int

    @Delete
    fun delete(member: Member)


    @Query("SELECT * FROM member WHERE id = :id")
    fun getMember(id: Long): LiveData<Member>

    @Query("DELETE FROM member")
    fun deleteAll()

    @Query("SELECT * FROM member WHERE householdId = :householdId")
    fun getMemberByHouseHold(householdId: String): LiveData<Member>

    @Query("SELECT * FROM member")
    fun getMembers(): LiveData<List<Member>>

    @Query("SELECT * FROM member WHERE householdId = :householdId AND registed=:registed")
    fun getHouseHoldMembers(householdId: String, registed: Boolean = false): LiveData<List<Member>>

    @Query("SELECT * FROM member INNER JOIN household_request ON member.householdId == household_request.id where household_request.enumeration_id = :householdQR")
    fun getHouseHoldMemberQr(householdQR: String): LiveData<List<Member>>


    @Query("UPDATE member SET registed =:registed WHERE uuid = :id")
    fun updateRegisted(id: String, registed: Boolean): Long


//    SELECT * FROM repo INNER JOIN user_repo_join ON
//    repo.id=user_repo_join.repoId WHERE
//    user_repo_join.userId=:userId


}
