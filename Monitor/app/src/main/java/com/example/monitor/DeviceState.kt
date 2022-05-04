package com.example.monitor

import androidx.room.*

@Entity(tableName = "device_state")
data class DeviceState(
    @PrimaryKey(true) val id: Int,
    val state: String,
    val date: String,
    val time: String
)

@Dao
interface DeviceStateDao {
    @Query("SELECT * FROM device_state")
    fun getAll(): MutableList<DeviceState>

    @Insert
    fun insert(data: DeviceState)

    @Query("DELETE FROM device_state")
    fun clear()
}

@Database(entities = [DeviceState::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getDao(): DeviceStateDao
}


