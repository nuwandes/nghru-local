package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.SampleProcess

/**
 * Interface for database access for User related operations.
 */
@Dao
interface SampleProcessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sampleProcess: SampleProcess): Long

    @Update
    fun update(sampleProcess: SampleProcess): Int

    @Delete
    fun delete(sampleProcess: SampleProcess)

    @Query("SELECT * FROM sample_process WHERE id = :id")
    fun getSampleProcess(id: Long): LiveData<SampleProcess>

    @Query("SELECT * FROM sample_process")
    fun getSampleProcesss(): LiveData<List<SampleProcess>>

}
