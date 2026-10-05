package com.example.turnover.model

data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val isImprovementAction: Boolean = false,
    val overcomesFailureId: String? = null,
    val targetFailureDescription: String? = null,
    val estimatedMinutes: Int = 25,
    val category: String = "今日の学習",
    val priority: String = "medium", // "high", "medium", "low"
    val completed: Boolean = false,
    val completedAt: String? = null,
    val userNote: String? = null,
    val improvementPointAwarded: Boolean = false
)
