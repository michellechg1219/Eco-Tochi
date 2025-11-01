package com.example.ecotochi.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ecotochi.data.db.dao.HistoricalDao
import com.example.ecotochi.data.db.dao.SettingsDao
import com.example.ecotochi.data.db.entity.HistoricalReading
import com.example.ecotochi.data.db.entity.SettingsEntity

@Database(
    entities = [HistoricalReading::class, SettingsEntity::class],
    version = 3,                  // ← sube a 3 (1→2: ph; 2→3: ph_max/ph_min)
    exportSchema = true
)
abstract class GreenhouseDatabase : RoomDatabase() {
    abstract fun historicalDao(): HistoricalDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile private var INSTANCE: GreenhouseDatabase? = null

        // 1 → 2: agregar 'ph' a historical_readings
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE historical_readings " +
                            "ADD COLUMN ph REAL NOT NULL DEFAULT 7.0"
                )
            }
        }

        // 2 → 3: agregar 'ph_max' y 'ph_min' a settings
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE settings " +
                            "ADD COLUMN ph_max REAL NOT NULL DEFAULT 8.0"
                )
                db.execSQL(
                    "ALTER TABLE settings " +
                            "ADD COLUMN ph_min REAL NOT NULL DEFAULT 6.0"
                )
            }
        }

        fun getInstance(context: Context): GreenhouseDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GreenhouseDatabase::class.java,
                    "greenhouse"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // ← aplica migraciones
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
