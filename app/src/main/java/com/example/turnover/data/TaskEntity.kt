package com.example.turnover.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.turnover.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isImprovementAction: Boolean,
    val overcomesFailureId: String?,
    val targetFailureDescription: String?,
    val estimatedMinutes: Int,
    val category: String,
    val priority: String,
    val completed: Boolean,
    val completedAt: String?,
    val userNote: String?,
    val improvementPointAwarded: Boolean,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): Task = Task(
        id = id,
        title = title,
        description = description,
        isImprovementAction = isImprovementAction,
        overcomesFailureId = overcomesFailureId,
        targetFailureDescription = targetFailureDescription,
        estimatedMinutes = estimatedMinutes,
        category = category,
        priority = priority,
        completed = completed,
        completedAt = completedAt,
        userNote = userNote,
        improvementPointAwarded = improvementPointAwarded
    )

    companion object {
        fun fromModel(task: Task, createdAt: Long = System.currentTimeMillis()): TaskEntity = TaskEntity(
            id = task.id,
            title = task.title,
            description = task.description,
            isImprovementAction = task.isImprovementAction,
            overcomesFailureId = task.overcomesFailureId,
            targetFailureDescription = task.targetFailureDescription,
            estimatedMinutes = task.estimatedMinutes,
            category = task.category,
            priority = task.priority,
            completed = task.completed,
            completedAt = task.completedAt,
            userNote = task.userNote,
            improvementPointAwarded = task.improvementPointAwarded,
            createdAt = createdAt
        )
    }
}
