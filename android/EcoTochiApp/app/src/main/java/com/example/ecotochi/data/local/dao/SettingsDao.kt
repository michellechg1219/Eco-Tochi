package com.example.ecotochi.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ecotochi.data.db.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    // Para observar en tiempo real desde la pantalla / ViewModel
    @Query("SELECT * FROM settings LIMIT 1")
    fun observeSettings(): Flow<SettingsEntity?>

    // 👇 Para pedir el último registro desde una corrutina (lo usa HomeViewModel y el Worker)
    @Query("SELECT * FROM settings ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: SettingsEntity)

    @Query("DELETE FROM settings")
    suspend fun clear()
}
