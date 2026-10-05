package com.example.turnover.network

import com.example.turnover.BuildConfig
import com.example.turnover.model.ExtractedFailure
import com.example.turnover.model.FailureItem
import com.example.turnover.model.ProposedImprovement
import com.example.turnover.model.ReflectionAnalysisResult
import com.example.turnover.model.Task
import com.example.turnover.model.TaskMatch
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class GeminiAnalysisService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {
    suspend fun analyzeReflection(
        journalText: String,
        currentTasks: List<Task>,
        unresolvedFailures: List<FailureItem>
    ): ReflectionAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank()) {
            return@withContext heuristicAnalyzeReflection(journalText, currentTasks)
        }

        try {
            val tasksPrompt = if (currentTasks.isNotEmpty()) {
                currentTasks.joinToString("\n") { "- [ID: ${it.id}] ${it.title} (状態: ${if (it.completed) "完了済" else "未完了"})" }
            } else {
                "（当日の事前タスクなし）"
            }

            val failuresPrompt = if (unresolvedFailures.isNotEmpty()) {
                unresolvedFailures.joinToString("\n") { "- [ID: ${it.id}] ${it.failure}" }
            } else {
                "（過去の未克服失敗なし）"
            }

            val prompt = """
                あなたは「質の高い学び」を実現する学習改善コーチAIです。教科や内容を問わず、ユーザーが自由に入力した振り返りをそのまま真摯に受け止め、中学生にも直感的にわかる平易で親切な日本語で分析してください。
                ユーザーが夜に書いた振り返りテキストを分析し、以下のステップでJSON形式で構造化してください：
                
                1. 成功した点 (extractedAchievements): できたこと・良かったこと（配列）
                2. つまずき・失敗した点 (extractedFailures): できなかったこと、ミス、課題（id, failure, context, severity, rootCause）
                3. 明日の予定 (extractedTomorrowPlans): 明日やりたいことやテスト勉強等（配列）
                4. タスク照合 (taskMatches): 今日のタスクと照合判定（taskId, status, evidence）
                5. 改善提案 (proposedImprovements): 明日すぐ実行できるスモールステップ対策（id, targetFailure, actionTitle, actionDetail, estimatedMinutes, selected: true）
                6. 激励メッセージ (encouragementNote): 失敗を振り返られたことを褒める短い言葉
                
                【本日のタスク】:
                $tasksPrompt
                
                【過去の未克服の課題】:
                $failuresPrompt
                
                【ユーザーの振り返り】:
                \"\"\"
                $journalText
                \"\"\"
            """.trimIndent()

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val requestJson = JsonObject().apply {
                val contents = com.google.gson.JsonArray().apply {
                    add(JsonObject().apply {
                        val parts = com.google.gson.JsonArray().apply {
                            add(JsonObject().apply { addProperty("text", prompt) })
                        }
                        add("parts", parts)
                    })
                }
                add("contents", contents)
                val generationConfig = JsonObject().apply {
                    addProperty("responseMimeType", "application/json")
                }
                add("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext heuristicAnalyzeReflection(journalText, currentTasks)
            }

            val parsedResponse = gson.fromJson(responseBody, JsonObject::class.java)
            val candidates = parsedResponse.getAsJsonArray("candidates")
            val candidateText = candidates?.get(0)?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")?.get(0)?.asJsonObject
                ?.get("text")?.asString

            if (!candidateText.isNullOrBlank()) {
                val result = gson.fromJson(candidateText, ReflectionAnalysisResult::class.java)
                if (result != null && (result.extractedAchievements.isNotEmpty() || result.extractedFailures.isNotEmpty() || result.proposedImprovements.isNotEmpty())) {
                    return@withContext result
                }
            }

            heuristicAnalyzeReflection(journalText, currentTasks)
        } catch (e: Exception) {
            heuristicAnalyzeReflection(journalText, currentTasks)
        }
    }

    fun heuristicAnalyzeReflection(
        rawText: String,
        currentTasks: List<Task>
    ): ReflectionAnalysisResult {
        val lines = rawText.split(Pattern.compile("[。\n]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val achievements = mutableListOf<String>()
        val failures = mutableListOf<ExtractedFailure>()
        val tomorrowPlans = mutableListOf<String>()

        val tomorrowRegex = Pattern.compile("明日|あした|次回|今度|テスト|予定|やりたい|計画|復習したい|勉強したい|対策したい")
        val negRegex = Pattern.compile("できな|失敗|難し|わからな|詰まっ|集中でき|時間足り|解けな|ケアレス|ミス|間違|眠か|調整|遅れ|忘断|抜け|エラー|バグ|動かな|落とし")
        val posRegex = Pattern.compile("できた|完了|理解できた|解けた|読んだ|解き切った|進んだ|よかった|勉強した|起きて|スムーズ|書けた|成功|作れた|解いた")

        for (line in lines) {
            val cleanLine = line.replace(Regex("^[・\\-\\*\\d\\.\\s]+"), "").trim()
            if (cleanLine.isBlank()) continue

            when {
                tomorrowRegex.matcher(line).find() -> tomorrowPlans.add(cleanLine)
                negRegex.matcher(line).find() -> {
                    val isCritical = line.contains("5回") || line.contains("全然") || line.contains("何度も")
                    val rootCause = when {
                        line.contains("眠") -> "集中環境・コンディション"
                        line.contains("時間") -> "時間配分の課題"
                        line.contains("間違") || line.contains("ミス") -> "理解不足・確認漏れ"
                        else -> "演習・実践不足"
                    }
                    failures.add(
                        ExtractedFailure(
                            id = "f_" + UUID.randomUUID().toString().substring(0, 8),
                            failure = cleanLine,
                            context = "振り返り記録からの抽出",
                            severity = if (isCritical) "critical" else "moderate",
                            rootCause = rootCause
                        )
                    )
                }
                posRegex.matcher(line).find() -> achievements.add(cleanLine)
            }
        }

        if (achievements.isEmpty()) {
            if (lines.isNotEmpty()) {
                achievements.add(lines[0].replace(Regex("^[・\\-\\*\\d\\.\\s]+"), "").trim())
            } else {
                achievements.add("今日の学習・作業に取り組み振り返りを記録したこと")
            }
        }

        if (failures.isEmpty() && lines.size > 1) {
            failures.add(
                ExtractedFailure(
                    id = "f_" + UUID.randomUUID().toString().substring(0, 8),
                    failure = lines[1].replace(Regex("^[・\\-\\*\\d\\.\\s]+"), "").trim(),
                    context = "日々の振り返り",
                    severity = "minor",
                    rootCause = "さらなる定着・改善のための確認"
                )
            )
        }

        val taskMatches = currentTasks.map { t ->
            val titleLower = t.title.lowercase()
            val hitPos = achievements.any { it.lowercase().contains(titleLower) || titleLower.contains(it.lowercase().take(4)) }
            val hitNeg = failures.any { it.failure.lowercase().contains(titleLower) || titleLower.contains(it.failure.lowercase().take(4)) }
            val status = when {
                hitPos && !hitNeg -> "completed"
                hitNeg -> "not_completed"
                else -> "ambiguous"
            }
            TaskMatch(
                taskId = t.id,
                status = status,
                evidence = when (status) {
                    "completed" -> "記録に完了の記述あり"
                    "not_completed" -> "課題・つまずきの記録あり"
                    else -> "言及が見当たらないため要確認"
                }
            )
        }

        val proposedImprovements = mutableListOf<ProposedImprovement>()
        failures.forEachIndexed { idx, f ->
            val shortTitle = if (f.failure.length > 25) f.failure.take(25) + "..." else f.failure
            proposedImprovements.add(
                ProposedImprovement(
                    id = "imp_${idx + 1}",
                    targetFailure = f.failure,
                    actionTitle = "${shortTitle}の対策・見直し",
                    actionDetail = "${f.failure}を意識して、手順を確認しながら丁寧に取り組む",
                    estimatedMinutes = 20,
                    selected = true
                )
            )
        }

        tomorrowPlans.forEachIndexed { idx, plan ->
            proposedImprovements.add(
                ProposedImprovement(
                    id = "imp_plan_${idx + 1}",
                    targetFailure = "明日の予定",
                    actionTitle = plan,
                    actionDetail = "計画に沿って集中して取り組む",
                    estimatedMinutes = 30,
                    selected = true
                )
            )
        }

        if (proposedImprovements.isEmpty()) {
            proposedImprovements.add(
                ProposedImprovement(
                    id = "imp_default_1",
                    targetFailure = "本日の復習",
                    actionTitle = "今日学んだ内容の要点を見直す",
                    actionDetail = "重要ポイントや間違えた箇所の解き直しを行う",
                    estimatedMinutes = 20,
                    selected = true
                )
            )
        }

        return ReflectionAnalysisResult(
            extractedAchievements = achievements,
            extractedFailures = failures,
            extractedTomorrowPlans = tomorrowPlans,
            taskMatches = taskMatches,
            proposedImprovements = proposedImprovements,
            encouragementNote = "できたことと改善したい点を素直に書き出せました！明日のアクションで克服していきましょう。"
        )
    }
}
