package com.example.turnover.model

data class ExtractedFailure(
    val id: String,
    val failure: String,
    val context: String = "振り返り記録からの抽出",
    val severity: String = "moderate",
    val rootCause: String = "原因を分析中"
)

data class TaskMatch(
    val taskId: String,
    val status: String, // "completed", "not_completed", "ambiguous"
    val evidence: String
)

data class ProposedImprovement(
    val id: String,
    val targetFailure: String,
    val actionTitle: String,
    val actionDetail: String,
    val estimatedMinutes: Int = 20,
    val selected: Boolean = true
)

data class ReflectionAnalysisResult(
    val extractedAchievements: List<String> = emptyList(),
    val extractedFailures: List<ExtractedFailure> = emptyList(),
    val extractedTomorrowPlans: List<String> = emptyList(),
    val taskMatches: List<TaskMatch> = emptyList(),
    val proposedImprovements: List<ProposedImprovement> = emptyList(),
    val encouragementNote: String = "できたことと改善したい点を素直に書き出せました！明日のアクションで克服していきましょう。"
)
