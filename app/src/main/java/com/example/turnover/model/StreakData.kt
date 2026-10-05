package com.example.turnover.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayRecord(
    val dayName: String,
    val date: String,
    val completed: Boolean = false,
    val isToday: Boolean = false
)

data class StreakData(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastReflectionDate: String? = null,
    val weeklyHistory: List<DayRecord> = emptyList()
) {
    companion object {
        fun initial(): StreakData {
            val dayNames = listOf("日", "月", "火", "水", "木", "金", "土")
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
            val today = Calendar.getInstance()
            val todayStr = sdf.format(today.time)
            val dayOfWeek = today.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...

            // Monday offset
            val mondayOffset = if (dayOfWeek == Calendar.SUNDAY) -6 else 2 - dayOfWeek
            val monday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, mondayOffset)
            }

            val records = (0 until 7).map { i ->
                val d = (monday.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, i)
                }
                val dateStr = sdf.format(d.time)
                val dayNameIdx = d.get(Calendar.DAY_OF_WEEK) - 1
                DayRecord(
                    dayName = dayNames[dayNameIdx],
                    date = dateStr,
                    completed = false,
                    isToday = (dateStr == todayStr)
                )
            }

            return StreakData(
                currentStreak = 0,
                bestStreak = 0,
                lastReflectionDate = null,
                weeklyHistory = records
            )
        }
    }
}

data class UserStats(
    val overcomeFailuresCount: Int = 0,
    val totalLoggedFailures: Int = 0,
    val currentStreak: Int = 0,
    val improvementPoints: Double = 0.0
)
