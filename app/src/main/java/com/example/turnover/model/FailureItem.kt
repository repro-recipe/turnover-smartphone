package com.example.turnover.model

data class FailureItem(
    val id: String,
    val date: String,
    val failure: String,
    val context: String = "日々の学習",
    val severity: String = "moderate", // "minor", "moderate", "critical"
    val rootCause: String = "原因を分析中",
    val status: String = "unresolved", // "unresolved", "in_action", "overcome"
    val improvementActionTitle: String? = null,
    val improvementActionDetail: String? = null,
    val overcomeDate: String? = null,
    val overcomeReflection: String? = null,
    val praiseNote: String? = null
)
