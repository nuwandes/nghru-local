package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.Meta


@Dao
interface MetaNewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(meta: Meta): Long

    @Update
    fun update(meta: Meta): Int

    @Delete
    fun delete(meta: Meta)


    @Query("SELECT * FROM meta_new WHERE id = :id")
    fun getNewMeta(id: Long): Meta

    @Query("SELECT * FROM meta_new")
    fun getNewMetas(): LiveData<List<Meta>>

}