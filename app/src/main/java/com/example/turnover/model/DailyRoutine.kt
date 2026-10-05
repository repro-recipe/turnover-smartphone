package com.example.turnover.model

data class DailyRoutine(
    val id: String,
    val title: String,
    val description: String? = null,
    val streakCount: Int = 0,
    val completedToday: Boolean = false,
    val lastCompletedDate: String? = null,
    val pointAwardedToday: Boolean = false,
    val createdAt: String = ""
)
