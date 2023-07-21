package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.southasia.ghru.vo.Asset

/**
 * Interface for database access for User related operations.
 */
@Dao
interface AssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(asset: Asset): Long

    @Update
    fun update(asset: Asset): Int

    @Delete
    fun delete(asset: Asset)

    @Query("SELECT * FROM asset WHERE id = :id")
    fun getAsset(id: Long): LiveData<Asset>

    @Query("SELECT * FROM asset")
    fun getAssets(): LiveData<List<Asset>>

}
