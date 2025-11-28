package com.example.ecotochi.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Long = 1L, // único registro de configuración

    @ColumnInfo(name = "temperature_max") val temperatureMax: Double,
    @ColumnInfo(name = "temperature_min") val temperatureMin: Double,
    @ColumnInfo(name = "wet_max")         val wetMax: Double,
    @ColumnInfo(name = "wet_min")         val wetMin: Double,
    @ColumnInfo(name = "reading_time")    val readingTime: Long,
    @ColumnInfo(name = "ph_max")          val phMax: Double,
    @ColumnInfo(name = "ph_min")          val phMin: Double,

    // Nueva bandera: true = automático, false = manual
    @ColumnInfo(name = "automatic_irrigation")
    val automaticIrrigation: Boolean = false
)
