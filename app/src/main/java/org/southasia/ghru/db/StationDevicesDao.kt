package org.southasia.ghru.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.southasia.ghru.vo.StationDeviceData

@Dao
interface StationDevicesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stationDeviceData: StationDeviceData): Long

    @Insert
    fun insertAll(deviceList: List<StationDeviceData>)

    @Query("DELETE FROM station_devices")
    fun deleteAll()

    @Query("SELECT * FROM station_devices WHERE id = :id")
    fun getDevice(id: Int): LiveData<StationDeviceData>

    @Query("SELECT * FROM station_devices")
    fun getAllDevice(): LiveData<List<StationDeviceData>>

    @Query("SELECT * FROM station_devices WHERE measurement = :measurement")
    fun stationDeviceList(measurement: String): LiveData<List<StationDeviceData>>

}