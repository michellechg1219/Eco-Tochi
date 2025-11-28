package com.example.ecotochi.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ecotochi.data.db.entity.HistoricalReading
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricalDao {
    @Query("SELECT * FROM historical_readings ORDER BY date DESC")
    fun getAll(): Flow<List<HistoricalReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: HistoricalReading)

    @Query("DELETE FROM historical_readings")
    suspend fun clear()
}
