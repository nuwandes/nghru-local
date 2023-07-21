package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.SampleData

/**
 * Interface for database access for User related operations.
 */
@Dao
interface SampleDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sampleData: SampleData): Long

    @Update
    fun update(sampleData: SampleData): Int

    @Delete
    fun delete(sampleData: SampleData)

    @Query("SELECT * FROM sample_data WHERE id = :id")
    fun getSampleData(id: Long): LiveData<SampleData>

    @Query("SELECT * FROM sample_data")
    fun getSampleDatas(): LiveData<List<SampleData>>

}
