package com.example.turnover.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.turnover.model.DayRecord
import com.example.turnover.model.StreakData
import com.example.turnover.model.UserStats

@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int,
    val bestStreak: Int,
    val lastReflectionDate: String?,
    val weeklyHistoryJson: String,
    val overcomeFailuresCount: Int,
    val totalLoggedFailures: Int,
    val improvementPoints: Double
)
