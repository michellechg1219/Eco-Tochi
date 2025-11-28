package com.example.ecotochi.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ecotochi.data.db.entity.SettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DbProvider {
    @Volatile private var INSTANCE: GreenhouseDatabase? = null

    fun get(context: Context): GreenhouseDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: build(context).also { INSTANCE = it }
        }
    }

    private fun build(context: Context): GreenhouseDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            GreenhouseDatabase::class.java,
            "greenhouse"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        // valores por defecto para settings
                        get(context).settingsDao().upsert(
                            SettingsEntity(
                                id = 1,
                                temperatureMax = 30.0,
                                temperatureMin = 18.0,
                                wetMax = 80.0,
                                wetMin = 40.0,
                                readingTime = 60,
                                phMax = 8.0,
                                phMin = 6.0
                            )
                        )
                    }
                }
            })
            .fallbackToDestructiveMigration() // solo en dev
            .build()
    }
}
