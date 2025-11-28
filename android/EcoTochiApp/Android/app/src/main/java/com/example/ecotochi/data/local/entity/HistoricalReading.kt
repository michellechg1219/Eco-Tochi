package com.example.ecotochi.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historical_readings")
data class HistoricalReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "temperature") val temperature: Double,
    @ColumnInfo(name = "wet") val wet: Double,           // humedad (%)
    @ColumnInfo(name = "date") val date: String,         // ISO-8601 o tu formato preferido
    @ColumnInfo(name = "ph") val ph: Double = 7.0        // NUEVO: pH con default
)
