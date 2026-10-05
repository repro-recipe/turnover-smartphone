package com.example.turnover.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        DailyRoutineEntity::class,
        FailureEntity::class,
        AppMetaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun dailyRoutineDao(): DailyRoutineDao
    abstract fun failureDao(): FailureDao
    abstract fun appMetaDao(): AppMetaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "turnover_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
