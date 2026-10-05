package com.example.turnover.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.turnover.data.AppDatabase
import com.example.turnover.data.TurnOverRepository
import com.example.turnover.model.BackupData
import com.example.turnover.model.BackupPayload
import com.example.turnover.model.DailyRoutine
import com.example.turnover.model.FailureItem
import com.example.turnover.model.ProposedImprovement
import com.example.turnover.model.ReflectionAnalysisResult
import com.example.turnover.model.StreakData
import com.example.turnover.model.Task
import com.example.turnover.model.UserStats
import com.example.turnover.network.GeminiAnalysisService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    HOME,
    ARCHIVE,
    REFLECTION
}

class TurnOverViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TurnOverRepository(
        taskDao = database.taskDao(),
        dailyRoutineDao = database.dailyRoutineDao(),
        failureDao = database.failureDao(),
        appMetaDao = database.appMetaDao()
    )
    private val geminiService = GeminiAnalysisService()
    private val gson = Gson()

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyRoutines: StateFlow<List<DailyRoutine>> = repository.dailyRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val failures: StateFlow<List<FailureItem>> = repository.failures
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streak: StateFlow<StreakData> = repository.streakData
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakData.initial())

    val stats: StateFlow<UserStats> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStats(0, 0, 0, 0.0))

    // Navigation & Modal States
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val _isBackupDialogOpen = MutableStateFlow(false)
    val isBackupDialogOpen: StateFlow<Boolean> = _isBackupDialogOpen.asStateFlow()

    // Reflection Wizard States
    private val _reflectionStep = MutableStateFlow(1) // 1: Reflect, 2: Analyze, 3: Plan, 4: Confirm
    val reflectionStep: StateFlow<Int> = _reflectionStep.asStateFlow()

    private val _journalText = MutableStateFlow("")
    val journalText: StateFlow<String> = _journalText.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<ReflectionAnalysisResult?>(null)
    val analysisResult: StateFlow<ReflectionAnalysisResult?> = _analysisResult.asStateFlow()

    private val _selectedImprovements = MutableStateFlow<List<ProposedImprovement>>(emptyList())
    val selectedImprovements: StateFlow<List<ProposedImprovement>> = _selectedImprovements.asStateFlow()

    private val _confirmedNextDayTasks = MutableStateFlow<List<Task>>(emptyList())
    val confirmedNextDayTasks: StateFlow<List<Task>> = _confirmedNextDayTasks.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openBackupDialog(open: Boolean) {
        _isBackupDialogOpen.value = open
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    // Task Actions
    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            val toast = repository.toggleTask(taskId)
            if (toast.isNotBlank()) showToast(toast)
        }
    }

    fun addTask(
        title: String,
        description: String,
        isImprovement: Boolean,
        targetFailure: String?,
        estimatedMinutes: Int,
        category: String,
        priority: String
    ) {
        viewModelScope.launch {
            val task = Task(
                id = "task_${System.currentTimeMillis()}",
                title = title.trim(),
                description = description.trim(),
                isImprovementAction = isImprovement,
                targetFailureDescription = if (isImprovement) targetFailure?.trim() else null,
                estimatedMinutes = estimatedMinutes,
                category = category,
                priority = priority,
                completed = false
            )
            repository.addTask(task)
            showToast("新しいタスクを追加しました")
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            showToast("タスクを削除しました")
        }
    }

    fun updateTaskNote(taskId: String, note: String) {
        viewModelScope.launch {
            repository.updateTaskNote(taskId, note.trim())
            showToast("ひとことメモを保存しました（夜のふりかえりに反映されます）")
        }
    }

    // Daily Routine Actions
    fun toggleDailyRoutine(routineId: String) {
        viewModelScope.launch {
            val toast = repository.toggleDailyRoutine(routineId)
            if (toast.isNotBlank()) showToast(toast)
        }
    }

    fun addDailyRoutine(title: String, description: String?) {
        viewModelScope.launch {
            repository.addDailyRoutine(title.trim(), description?.trim())
            showToast("新しいデイリータスクを登録しました（毎日継続3日以上で0.5pt獲得）")
        }
    }

    fun deleteDailyRoutine(routineId: String) {
        viewModelScope.launch {
            repository.deleteDailyRoutine(routineId)
            showToast("デイリータスクを削除しました")
        }
    }

    fun editDailyRoutine(routineId: String, title: String, description: String?) {
        viewModelScope.launch {
            repository.editDailyRoutine(routineId, title.trim(), description?.trim())
            showToast("デイリータスクを更新しました")
        }
    }

    // Failure Overcome Actions
    fun markFailureOvercome(failureId: String, reflection: String) {
        viewModelScope.launch {
            repository.markFailureOvercome(failureId, reflection.trim())
            showToast("克服ノートに記録しました！おめでとうございます (+1pt)")
        }
    }

    fun addFailure(failure: String, context: String, rootCause: String, severity: String) {
        viewModelScope.launch {
            val item = FailureItem(
                id = "f_${UUID.randomUUID().toString().substring(0, 8)}",
                date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.JAPAN).format(java.util.Date()),
                failure = failure.trim(),
                context = context.trim().ifBlank { "日々の学習" },
                severity = severity,
                rootCause = rootCause.trim().ifBlank { "原因を分析中" },
                status = "unresolved"
            )
            repository.addFailure(item)
            showToast("つまずきを克服ノートに登録しました")
        }
    }

    // Direct Next Day Progression
    fun advanceToNextDayDirect() {
        viewModelScope.launch {
            val toast = repository.advanceToNextDayDirect()
            showToast(toast)
        }
    }

    // Evening Reflection Flow
    fun startEveningReflection() {
        _reflectionStep.value = 1
        _journalText.value = ""
        _analysisResult.value = null
        _selectedImprovements.value = emptyList()
        _confirmedNextDayTasks.value = emptyList()
        _currentScreen.value = AppScreen.REFLECTION
    }

    fun setJournalText(text: String) {
        _journalText.value = text
    }

    fun setReflectionStep(step: Int) {
        _reflectionStep.value = step
    }

    fun analyzeReflection() {
        if (_journalText.value.isBlank()) return
        _isAnalyzing.value = true
        viewModelScope.launch {
            try {
                val currentTasks = tasks.value
                val unresolvedFailures = failures.value.filter { it.status != "overcome" }
                val result = geminiService.analyzeReflection(_journalText.value, currentTasks, unresolvedFailures)
                _analysisResult.value = result
                _selectedImprovements.value = result.proposedImprovements.map { it.copy(selected = true) }
                _reflectionStep.value = 2
            } catch (e: Exception) {
                showToast("分析に失敗しました。もう一度お試しください。")
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun toggleImprovementSelection(improvementId: String) {
        _selectedImprovements.value = _selectedImprovements.value.map { imp ->
            if (imp.id == improvementId) imp.copy(selected = !imp.selected) else imp
        }
    }

    fun addCustomImprovement(title: String) {
        if (title.isBlank()) return
        val newImp = ProposedImprovement(
            id = "custom_${System.currentTimeMillis()}",
            targetFailure = "自由設定",
            actionTitle = title.trim(),
            actionDetail = "自分で設定した明日やる対策",
            estimatedMinutes = 20,
            selected = true
        )
        _selectedImprovements.value = _selectedImprovements.value + newImp
    }

    fun prepareConfirmedTasks() {
        val selected = _selectedImprovements.value.filter { it.selected }
        val generatedTasks = mutableListOf<Task>()

        selected.forEachIndexed { idx, imp ->
            generatedTasks.add(
                Task(
                    id = "task_conf_${idx}_${System.currentTimeMillis()}",
                    title = imp.actionTitle,
                    description = imp.actionDetail.ifBlank { "つまずき対策スモールステップ" },
                    isImprovementAction = true,
                    targetFailureDescription = imp.targetFailure,
                    estimatedMinutes = imp.estimatedMinutes,
                    category = "改善アクション",
                    priority = "high",
                    completed = false
                )
            )
        }

        val analysis = _analysisResult.value
        analysis?.extractedTomorrowPlans?.forEachIndexed { i, plan ->
            if (generatedTasks.none { it.title.contains(plan) || plan.contains(it.title) }) {
                generatedTasks.add(
                    Task(
                        id = "task_plan_${i}_${System.currentTimeMillis()}",
                        title = plan,
                        description = "前日の振り返りで計画した学習・作業",
                        isImprovementAction = false,
                        estimatedMinutes = 30,
                        category = "予定・テスト対策",
                        priority = "medium",
                        completed = false
                    )
                )
            }
        }

        _confirmedNextDayTasks.value = generatedTasks
        _reflectionStep.value = 4
    }

    fun updateConfirmedTask(taskId: String, newTitle: String, newMinutes: Int) {
        _confirmedNextDayTasks.value = _confirmedNextDayTasks.value.map {
            if (it.id == taskId) it.copy(title = newTitle, estimatedMinutes = newMinutes) else it
        }
    }

    fun removeConfirmedTask(taskId: String) {
        _confirmedNextDayTasks.value = _confirmedNextDayTasks.value.filter { it.id != taskId }
    }

    fun addConfirmedTask(title: String, minutes: Int, isImprovement: Boolean) {
        if (title.isBlank()) return
        _confirmedNextDayTasks.value = _confirmedNextDayTasks.value + Task(
            id = "task_custom_${System.currentTimeMillis()}",
            title = title.trim(),
            description = "追加したタスク",
            isImprovementAction = isImprovement,
            estimatedMinutes = minutes,
            category = if (isImprovement) "改善アクション" else "明日の学習",
            priority = if (isImprovement) "high" else "medium",
            completed = false
        )
    }

    fun finalizeReflection() {
        viewModelScope.launch {
            val analysis = _analysisResult.value
            val newFailures = (analysis?.extractedFailures ?: emptyList()).map { f ->
                FailureItem(
                    id = f.id,
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.JAPAN).format(java.util.Date()),
                    failure = f.failure,
                    context = f.context,
                    severity = f.severity,
                    rootCause = f.rootCause,
                    status = "in_action",
                    improvementActionTitle = _confirmedNextDayTasks.value.find { it.targetFailureDescription == f.failure }?.title
                )
            }

            val toast = repository.completeEveningReflection(
                nextDayTasks = _confirmedNextDayTasks.value,
                newFailures = newFailures,
                overcomeFailures = emptyList()
            )
            showToast(toast)
            _currentScreen.value = AppScreen.HOME
        }
    }

    // Export & Import
    suspend fun getExportDataString(): String {
        return repository.getExportJson()
    }

    fun importBackupDataString(jsonString: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val parsed = gson.fromJson(jsonString, BackupPayload::class.java)
                val data = parsed.data
                val success = repository.importData(data)
                if (success) {
                    showToast("データを正常にインポートしました！")
                } else {
                    showToast("インポートに失敗しました。JSON形式を確認してください。")
                }
                onComplete(success)
            } catch (e: Exception) {
                // Try fallback to direct BackupData
                try {
                    val directData = gson.fromJson(jsonString, BackupData::class.java)
                    val success = repository.importData(directData)
                    if (success) {
                        showToast("データを正常にインポートしました！")
                    } else {
                        showToast("インポートに失敗しました。")
                    }
                    onComplete(success)
                } catch (e2: Exception) {
                    showToast("無効なJSONフォーマットです")
                    onComplete(false)
                }
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
            showToast("データを初期化しました")
        }
    }
}
