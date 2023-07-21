package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.AccessToken


/**
 * Interface for database access for User related operations.
 */
@Dao
interface AccessTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(accessToken: AccessToken)

    @Query("SELECT * FROM access_token")
    fun fetchAllData(): List<AccessToken>

    @Query("SELECT * FROM access_token LIMIT 1")
    fun getAccessToken(): LiveData<AccessToken>

    @Query("DELETE FROM access_token")
    fun nukeTable(): Int

    @Update
    fun logout(accessToken: AccessToken): Int

    @Update
    fun login(accessToken: AccessToken): Int

    @Query("SELECT * FROM access_token where user_name=:userName")
    fun getTokerByEmail(userName: String): LiveData<AccessToken>

    @Query("SELECT * FROM access_token where user_name=:userName and password=:password")
    fun getTokerByEmailPasword(userName: String, password: String): LiveData<AccessToken>
}
