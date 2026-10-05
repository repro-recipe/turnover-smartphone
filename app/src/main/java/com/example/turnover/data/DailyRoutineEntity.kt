package com.example.turnover.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.turnover.model.DailyRoutine

@Entity(tableName = "daily_routines")
data class DailyRoutineEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val streakCount: Int,
    val completedToday: Boolean,
    val lastCompletedDate: String?,
    val pointAwardedToday: Boolean,
    val createdAt: String
) {
    fun toModel(): DailyRoutine = DailyRoutine(
        id = id,
        title = title,
        description = description,
        streakCount = streakCount,
        completedToday = completedToday,
        lastCompletedDate = lastCompletedDate,
        pointAwardedToday = pointAwardedToday,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(routine: DailyRoutine): DailyRoutineEntity = DailyRoutineEntity(
            id = routine.id,
            title = routine.title,
            description = routine.description,
            streakCount = routine.streakCount,
            completedToday = routine.completedToday,
            lastCompletedDate = routine.lastCompletedDate,
            pointAwardedToday = routine.pointAwardedToday,
            createdAt = routine.createdAt
        )
    }
}
