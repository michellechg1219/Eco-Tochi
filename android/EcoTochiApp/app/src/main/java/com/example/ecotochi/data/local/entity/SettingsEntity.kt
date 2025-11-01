package com.example.ecotochi.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "temperature_max") val temperatureMax: Double,
    @ColumnInfo(name = "temperature_min") val temperatureMin: Double,
    @ColumnInfo(name = "wet_max")         val wetMax: Double,
    @ColumnInfo(name = "wet_min")         val wetMin: Double,
    @ColumnInfo(name = "reading_time")    val readingTime: Long,
    @ColumnInfo(name = "ph_max")          val phMax: Double,
    @ColumnInfo(name = "ph_min")          val phMin: Double
)
