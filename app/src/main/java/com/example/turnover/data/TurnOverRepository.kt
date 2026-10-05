package com.example.turnover.data

import com.example.turnover.model.BackupData
import com.example.turnover.model.DayRecord
import com.example.turnover.model.DailyRoutine
import com.example.turnover.model.FailureItem
import com.example.turnover.model.StreakData
import com.example.turnover.model.Task
import com.example.turnover.model.UserStats
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TurnOverRepository(
    private val taskDao: TaskDao,
    private val dailyRoutineDao: DailyRoutineDao,
    private val failureDao: FailureDao,
    private val appMetaDao: AppMetaDao,
    private val gson: Gson = Gson()
) {
    val tasks: Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
        entities.map { it.toModel() }
    }

    val dailyRoutines: Flow<List<DailyRoutine>> = dailyRoutineDao.getAllRoutines().map { entities ->
        entities.map { it.toModel() }
    }

    val failures: Flow<List<FailureItem>> = failureDao.getAllFailures().map { entities ->
        entities.map { it.toModel() }
    }

    val streakData: Flow<StreakData> = appMetaDao.getAppMeta().map { meta ->
        if (meta == null) {
            StreakData.initial()
        } else {
            val listType = object : TypeToken<List<DayRecord>>() {}.type
            val history: List<DayRecord> = try {
                gson.fromJson(meta.weeklyHistoryJson, listType) ?: StreakData.initial().weeklyHistory
            } catch (e: Exception) {
                StreakData.initial().weeklyHistory
            }
            StreakData(
                currentStreak = meta.currentStreak,
                bestStreak = meta.bestStreak,
                lastReflectionDate = meta.lastReflectionDate,
                weeklyHistory = history
            )
        }
    }

    val userStats: Flow<UserStats> = appMetaDao.getAppMeta().map { meta ->
        if (meta == null) {
            UserStats(0, 0, 0, 0.0)
        } else {
            UserStats(
                overcomeFailuresCount = meta.overcomeFailuresCount,
                totalLoggedFailures = meta.totalLoggedFailures,
                currentStreak = meta.currentStreak,
                improvementPoints = meta.improvementPoints
            )
        }
    }

    private suspend fun getCurrentMeta(): AppMetaEntity {
        val current = appMetaDao.getAppMeta().first()
        return current ?: AppMetaEntity(
            id = 1,
            currentStreak = 0,
            bestStreak = 0,
            lastReflectionDate = null,
            weeklyHistoryJson = gson.toJson(StreakData.initial().weeklyHistory),
            overcomeFailuresCount = 0,
            totalLoggedFailures = 0,
            improvementPoints = 0.0
        )
    }

    suspend fun addTask(task: Task) {
        taskDao.insertTask(TaskEntity.fromModel(task))
    }

    suspend fun deleteTask(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun updateTaskNote(taskId: String, note: String) {
        val currentTasks = tasks.first()
        val target = currentTasks.find { it.id == taskId } ?: return
        taskDao.updateTask(TaskEntity.fromModel(target.copy(userNote = note)))
    }

    suspend fun toggleTask(taskId: String): String {
        val currentTasks = tasks.first()
        val target = currentTasks.find { it.id == taskId } ?: return ""
        val nextCompleted = !target.completed
        val sdfTime = SimpleDateFormat("HH:mm", Locale.JAPAN)
        val completedAt = if (nextCompleted) sdfTime.format(Date()) else null

        val updatedTask = target.copy(
            completed = nextCompleted,
            completedAt = completedAt
        )
        taskDao.updateTask(TaskEntity.fromModel(updatedTask))

        if (target.isImprovementAction) {
            val meta = getCurrentMeta()
            if (nextCompleted) {
                val nextPts = meta.improvementPoints + 1.0
                val nextOvercomes = meta.overcomeFailuresCount + 1
                appMetaDao.saveAppMeta(
                    meta.copy(
                        improvementPoints = nextPts,
                        overcomeFailuresCount = nextOvercomes
                    )
                )
                return "改善アクション達成！+1ポイント獲得（累計: ${nextPts.toInt()}pt）"
            } else {
                val nextPts = maxOf(0.0, meta.improvementPoints - 1.0)
                val nextOvercomes = maxOf(0, meta.overcomeFailuresCount - 1)
                appMetaDao.saveAppMeta(
                    meta.copy(
                        improvementPoints = nextPts,
                        overcomeFailuresCount = nextOvercomes
                    )
                )
                return "改善アクションの完了を取り消しました（-1pt）"
            }
        }
        return ""
    }

    suspend fun toggleDailyRoutine(routineId: String): String {
        val routines = dailyRoutines.first()
        val target = routines.find { it.id == routineId } ?: return ""

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        var pointsDelta = 0.0
        var toastMsg = ""

        val updated = if (!target.completedToday) {
            var nextStreak = 1
            if (target.lastCompletedDate == yesterdayStr || target.streakCount == 0) {
                nextStreak = target.streakCount + 1
            } else if (target.lastCompletedDate == todayStr) {
                nextStreak = target.streakCount
            } else {
                nextStreak = 1
            }

            val awardsPoint = nextStreak >= 3
            if (awardsPoint) {
                pointsDelta = 0.5
                toastMsg = "「${target.title}」${nextStreak}日連続達成！0.5ポイント獲得しました！"
            } else {
                val remaining = 3 - nextStreak
                toastMsg = "「${target.title}」を完了しました（現在${nextStreak}日連続。あと${remaining}日で毎回+0.5pt獲得！）"
            }

            target.copy(
                completedToday = true,
                streakCount = nextStreak,
                lastCompletedDate = todayStr,
                pointAwardedToday = awardsPoint
            )
        } else {
            if (target.pointAwardedToday) {
                pointsDelta = -0.5
                toastMsg = "「${target.title}」の完了を取り消しました（-0.5pt）"
            } else {
                toastMsg = "「${target.title}」の完了を取り消しました"
            }
            target.copy(
                completedToday = false,
                streakCount = maxOf(0, target.streakCount - 1),
                pointAwardedToday = false
            )
        }

        dailyRoutineDao.updateRoutine(DailyRoutineEntity.fromModel(updated))

        if (pointsDelta != 0.0) {
            val meta = getCurrentMeta()
            val nextPts = maxOf(0.0, ((meta.improvementPoints + pointsDelta) * 10).toInt() / 10.0)
            appMetaDao.saveAppMeta(meta.copy(improvementPoints = nextPts))
        }

        return toastMsg
    }

    suspend fun addDailyRoutine(title: String, description: String?) {
        val routine = DailyRoutine(
            id = "routine_${System.currentTimeMillis()}",
            title = title,
            description = description,
            streakCount = 0,
            completedToday = false,
            createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.JAPAN).format(Date())
        )
        dailyRoutineDao.insertRoutine(DailyRoutineEntity.fromModel(routine))
    }

    suspend fun deleteDailyRoutine(routineId: String) {
        dailyRoutineDao.deleteRoutineById(routineId)
    }

    suspend fun editDailyRoutine(routineId: String, title: String, description: String?) {
        val routines = dailyRoutines.first()
        val target = routines.find { it.id == routineId } ?: return
        dailyRoutineDao.updateRoutine(
            DailyRoutineEntity.fromModel(
                target.copy(title = title, description = description)
            )
        )
    }

    suspend fun addFailure(failure: FailureItem) {
        failureDao.insertFailure(FailureEntity.fromModel(failure))
        val meta = getCurrentMeta()
        appMetaDao.saveAppMeta(meta.copy(totalLoggedFailures = meta.totalLoggedFailures + 1))
    }

    suspend fun markFailureOvercome(failureId: String, reflection: String) {
        val currentFailures = failures.first()
        val target = currentFailures.find { it.id == failureId } ?: return
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
        val todayStr = sdf.format(Date())

        val updated = target.copy(
            status = "overcome",
            overcomeDate = todayStr,
            overcomeReflection = reflection,
            praiseNote = "苦手と正面から向き合い、改善のアクションを完了しました！"
        )
        failureDao.updateFailure(FailureEntity.fromModel(updated))

        val meta = getCurrentMeta()
        appMetaDao.saveAppMeta(
            meta.copy(
                overcomeFailuresCount = meta.overcomeFailuresCount + 1,
                improvementPoints = meta.improvementPoints + 1.0
            )
        )
    }

    suspend fun completeEveningReflection(
        nextDayTasks: List<Task>,
        newFailures: List<FailureItem>,
        overcomeFailures: List<Pair<String, String>>
    ): String {
        val currentMeta = getCurrentMeta()
        val newStreak = currentMeta.currentStreak + 1
        val newBest = maxOf(currentMeta.bestStreak, newStreak)

        val listType = object : TypeToken<List<DayRecord>>() {}.type
        val currentHistory: List<DayRecord> = try {
            gson.fromJson(currentMeta.weeklyHistoryJson, listType) ?: StreakData.initial().weeklyHistory
        } catch (e: Exception) {
            StreakData.initial().weeklyHistory
        }

        val currentIdx = currentHistory.indexOfFirst { it.isToday }.let { if (it != -1) it else 0 }
        val nextIdx = (currentIdx + 1) % currentHistory.size

        val updatedHistory = currentHistory.mapIndexed { idx, day ->
            when (idx) {
                currentIdx -> day.copy(completed = true, isToday = false)
                nextIdx -> day.copy(
                    completed = if (nextIdx == 0) false else day.completed,
                    isToday = true
                )
                else -> if (nextIdx == 0) day.copy(completed = false, isToday = false) else day
            }
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
        val todayStr = sdf.format(Date())

        // Update Daily Routines for next day
        val routines = dailyRoutines.first()
        val updatedRoutines = routines.map { r ->
            r.copy(
                completedToday = false,
                pointAwardedToday = false,
                streakCount = if (r.completedToday) r.streakCount else 0
            )
        }
        dailyRoutineDao.deleteAllRoutines()
        dailyRoutineDao.insertRoutines(updatedRoutines.map { DailyRoutineEntity.fromModel(it) })

        // Replace tasks with next day tasks (all uncompleted)
        taskDao.deleteAllTasks()
        taskDao.insertTasks(nextDayTasks.map { TaskEntity.fromModel(it.copy(completed = false)) })

        // Insert new failures
        val existingFailures = failures.first()
        val toAdd = newFailures.filter { nf -> existingFailures.none { it.failure == nf.failure } }
        toAdd.forEach { failureDao.insertFailure(FailureEntity.fromModel(it)) }

        // Overcome marked failures
        overcomeFailures.forEach { (failureId, note) ->
            val f = existingFailures.find { it.id == failureId }
            if (f != null) {
                failureDao.updateFailure(
                    FailureEntity.fromModel(
                        f.copy(
                            status = "overcome",
                            overcomeDate = todayStr,
                            overcomeReflection = note,
                            praiseNote = "苦手と正面から向き合い、改善のアクションを完了しました！"
                        )
                    )
                )
            }
        }

        // Save Meta
        val updatedMeta = currentMeta.copy(
            currentStreak = newStreak,
            bestStreak = newBest,
            lastReflectionDate = todayStr,
            weeklyHistoryJson = gson.toJson(updatedHistory),
            overcomeFailuresCount = currentMeta.overcomeFailuresCount + overcomeFailures.size,
            totalLoggedFailures = currentMeta.totalLoggedFailures + toAdd.size
        )
        appMetaDao.saveAppMeta(updatedMeta)

        return "ふりかえり完了！次の日に進みました（継続 ${newStreak}日目）。明日のタスクを準備しました！"
    }

    suspend fun advanceToNextDayDirect(): String {
        val currentTasks = tasks.first()
        val carriedOver = currentTasks.filter { !it.completed }
        return completeEveningReflection(
            nextDayTasks = carriedOver,
            newFailures = emptyList(),
            overcomeFailures = emptyList()
        )
    }

    suspend fun getExportJson(): String {
        val allTasks = tasks.first()
        val allRoutines = dailyRoutines.first()
        val allFailures = failures.first()
        val streak = streakData.first()
        val stats = userStats.first()

        val payload = com.example.turnover.model.BackupPayload(
            app = "TurnOver",
            version = 1,
            exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.JAPAN).format(Date()),
            data = BackupData(
                tasks = allTasks,
                dailyRoutines = allRoutines,
                failures = allFailures,
                streak = streak,
                stats = stats
            )
        )
        return gson.toJson(payload)
    }

    suspend fun importData(data: BackupData): Boolean {
        try {
            if (data.tasks.isNotEmpty()) {
                taskDao.deleteAllTasks()
                taskDao.insertTasks(data.tasks.map { TaskEntity.fromModel(it) })
            }
            if (data.dailyRoutines.isNotEmpty()) {
                dailyRoutineDao.deleteAllRoutines()
                dailyRoutineDao.insertRoutines(data.dailyRoutines.map { DailyRoutineEntity.fromModel(it) })
            }
            if (data.failures.isNotEmpty()) {
                failureDao.deleteAllFailures()
                failureDao.insertFailures(data.failures.map { FailureEntity.fromModel(it) })
            }
            if (data.streak != null || data.stats != null) {
                val current = getCurrentMeta()
                val newMeta = current.copy(
                    currentStreak = data.streak?.currentStreak ?: current.currentStreak,
                    bestStreak = data.streak?.bestStreak ?: current.bestStreak,
                    lastReflectionDate = data.streak?.lastReflectionDate ?: current.lastReflectionDate,
                    weeklyHistoryJson = if (data.streak != null) gson.toJson(data.streak.weeklyHistory) else current.weeklyHistoryJson,
                    overcomeFailuresCount = data.stats?.overcomeFailuresCount ?: current.overcomeFailuresCount,
                    totalLoggedFailures = data.stats?.totalLoggedFailures ?: current.totalLoggedFailures,
                    improvementPoints = data.stats?.improvementPoints ?: current.improvementPoints
                )
                appMetaDao.saveAppMeta(newMeta)
            }
            return true
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun resetAllData() {
        taskDao.deleteAllTasks()
        dailyRoutineDao.deleteAllRoutines()
        failureDao.deleteAllFailures()
        appMetaDao.deleteAllMeta()
        appMetaDao.saveAppMeta(
            AppMetaEntity(
                id = 1,
                currentStreak = 0,
                bestStreak = 0,
                lastReflectionDate = null,
                weeklyHistoryJson = gson.toJson(StreakData.initial().weeklyHistory),
                overcomeFailuresCount = 0,
                totalLoggedFailures = 0,
                improvementPoints = 0.0
            )
        )
    }
}
