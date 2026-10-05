package com.example.turnover.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.turnover.model.FailureItem

@Entity(tableName = "failures")
data class FailureEntity(
    @PrimaryKey val id: String,
    val date: String,
    val failure: String,
    val context: String,
    val severity: String,
    val rootCause: String,
    val status: String,
    val improvementActionTitle: String?,
    val improvementActionDetail: String?,
    val overcomeDate: String?,
    val overcomeReflection: String?,
    val praiseNote: String?,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): FailureItem = FailureItem(
        id = id,
        date = date,
        failure = failure,
        context = context,
        severity = severity,
        rootCause = rootCause,
        status = status,
        improvementActionTitle = improvementActionTitle,
        improvementActionDetail = improvementActionDetail,
        overcomeDate = overcomeDate,
        overcomeReflection = overcomeReflection,
        praiseNote = praiseNote
    )

    companion object {
        fun fromModel(item: FailureItem, createdAt: Long = System.currentTimeMillis()): FailureEntity = FailureEntity(
            id = item.id,
            date = item.date,
            failure = item.failure,
            context = item.context,
            severity = item.severity,
            rootCause = item.rootCause,
            status = item.status,
            improvementActionTitle = item.improvementActionTitle,
            improvementActionDetail = item.improvementActionDetail,
            overcomeDate = item.overcomeDate,
            overcomeReflection = item.overcomeReflection,
            praiseNote = item.praiseNote,
            createdAt = createdAt
        )
    }
}
