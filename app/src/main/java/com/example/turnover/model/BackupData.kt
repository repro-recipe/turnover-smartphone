package com.example.turnover.model

data class BackupPayload(
    val app: String = "TurnOver",
    val version: Int = 1,
    val exportedAt: String = "",
    val data: BackupData
)

data class BackupData(
    val tasks: List<Task> = emptyList(),
    val dailyRoutines: List<DailyRoutine> = emptyList(),
    val failures: List<FailureItem> = emptyList(),
    val streak: StreakData? = null,
    val stats: UserStats? = null
)
