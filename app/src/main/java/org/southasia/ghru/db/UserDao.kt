package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.southasia.ghru.vo.User


/**
 * Interface for database access for User related operations.
 */
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Query("SELECT * FROM user WHERE email = :email OR mobile = :email")
    fun findByLogin(email: String): LiveData<User>

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): LiveData<User>
}
