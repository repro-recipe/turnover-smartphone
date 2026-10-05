package com.example.turnover.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle2
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnover.model.DailyRoutine
import com.example.turnover.model.Task
import com.example.turnover.ui.components.AddEditHabitDialog
import com.example.turnover.ui.components.AddEditTaskDialog
import com.example.turnover.ui.components.AppHeader
import com.example.turnover.ui.components.BackupDialog
import com.example.turnover.ui.components.DailyHabitsCard
import com.example.turnover.ui.components.EditNoteDialog
import com.example.turnover.ui.components.StreakOverviewCard
import com.example.turnover.ui.components.TaskCard
import com.example.turnover.ui.theme.DadsBgCanvas
import com.example.turnover.ui.theme.DadsBlue
import com.example.turnover.ui.theme.DadsBlueSubtle
import com.example.turnover.ui.theme.DadsBorder
import com.example.turnover.ui.theme.DadsBorderLight
import com.example.turnover.ui.theme.DadsSuccess
import com.example.turnover.ui.theme.DadsTextMuted
import com.example.turnover.ui.theme.DadsTextPrimary
import com.example.turnover.ui.theme.DadsTextSecondary
import com.example.turnover.ui.viewmodel.AppScreen
import com.example.turnover.ui.viewmodel.TurnOverViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: TurnOverViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val dailyRoutines by viewModel.dailyRoutines.collectAsStateWithLifecycle()
    val streak by viewModel.streak.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isBackupOpen by viewModel.isBackupDialogOpen.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var taskFilter by remember { mutableStateOf("all") } // "all", "improvements", "completed"
    var isAddTaskOpen by remember { mutableStateOf(false) }
    var isAddHabitOpen by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<DailyRoutine?>(null) }
    var taskToEditNote by remember { mutableStateOf<Task?>(null) }
    var exportJsonString by remember { mutableStateOf("") }

    val completedCount = tasks.count { it.completed }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f

    val filteredTasks = tasks.filter { task ->
        when (taskFilter) {
            "improvements" -> task.isImprovementAction
            "completed" -> task.completed
            else -> true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                currentScreen = AppScreen.HOME,
                onNavigateHome = { },
                onOpenBackup = {
                    scope.launch {
                        exportJsonString = viewModel.getExportDataString()
                        viewModel.openBackupDialog(true)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DadsBgCanvas
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Streak and Stats Card
            item {
                StreakOverviewCard(
                    streak = streak,
                    stats = stats,
                    onStartReflection = { viewModel.startEveningReflection() },
                    onNavigateArchive = { viewModel.navigateTo(AppScreen.ARCHIVE) },
                    onAdvanceToNextDayDirect = { viewModel.advanceToNextDayDirect() }
                )
            }

            // Daily Habits Section
            item {
                DailyHabitsCard(
                    routines = dailyRoutines,
                    onToggleRoutine = { viewModel.toggleDailyRoutine(it) },
                    onOpenAddRoutine = {
                        routineToEdit = null
                        isAddHabitOpen = true
                    },
                    onEditRoutine = {
                        routineToEdit = it
                        isAddHabitOpen = true
                    },
                    onDeleteRoutine = { viewModel.deleteDailyRoutine(it) }
                )
            }

            // Failure Archive banner / link
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.ARCHIVE) }
                        .testTag("home_go_to_archive_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DadsBlueSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderSpecial,
                                    contentDescription = null,
                                    tint = DadsBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "できるようになったこと（克服ノート）",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DadsTextPrimary
                                )
                                Text(
                                    text = "過去のつまずきを克服してポイント獲得！ (現在 ${stats.overcomeFailuresCount}件克服)",
                                    fontSize = 11.sp,
                                    color = DadsTextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "移動",
                            tint = DadsBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Today's Tasks Section Header & Controls
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "今日の学習・改善アクション",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${completedCount}/${tasks.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DadsBlue
                            )
                        }

                        OutlinedButton(
                            onClick = { isAddTaskOpen = true },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("home_add_task_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "タスクを追加",
                                tint = DadsBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "タスク追加",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsBlue
                            )
                        }
                    }

                    // Progress Bar
                    if (tasks.isNotEmpty()) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DadsSuccess,
                            trackColor = Color(0xFFE5E7EB)
                        )
                    }

                    // Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "all" to "すべて (${tasks.size})",
                            "improvements" to "改善アクション (${tasks.count { it.isImprovementAction }})",
                            "completed" to "完了 (${completedCount})"
                        ).forEach { (key, label) ->
                            val isSelected = taskFilter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DadsBlue else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) DadsBlue else DadsBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { taskFilter = key }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                    .testTag("task_filter_$key")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else DadsTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, DadsBorderLight, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ListAlt,
                                contentDescription = null,
                                tint = DadsTextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (tasks.isEmpty()) {
                                    "今日のタスクはありません。「タスク追加」から登録するか、\n「夜のふりかえり」で明日のタスクを自動生成しましょう！"
                                } else {
                                    "条件に一致するタスクはありません。"
                                },
                                fontSize = 12.sp,
                                color = DadsTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleTask = { viewModel.toggleTask(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onOpenNoteDialog = { taskToEditNote = it }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Dialogs
    AddEditTaskDialog(
        isOpen = isAddTaskOpen,
        onDismiss = { isAddTaskOpen = false },
        onAddTask = { title, desc, isImprovement, targetFailure, minutes ->
            viewModel.addTask(
                title = title,
                description = desc,
                isImprovement = isImprovement,
                targetFailure = targetFailure,
                estimatedMinutes = minutes,
                category = if (isImprovement) "改善アクション" else "今日の学習",
                priority = if (isImprovement) "high" else "medium"
            )
        }
    )

    AddEditHabitDialog(
        isOpen = isAddHabitOpen,
        routineToEdit = routineToEdit,
        onDismiss = {
            isAddHabitOpen = false
            routineToEdit = null
        },
        onSave = { title, desc ->
            if (routineToEdit != null) {
                viewModel.editDailyRoutine(routineToEdit!!.id, title, desc)
            } else {
                viewModel.addDailyRoutine(title, desc)
            }
        }
    )

    EditNoteDialog(
        task = taskToEditNote,
        onDismiss = { taskToEditNote = null },
        onSaveNote = { note ->
            taskToEditNote?.let { viewModel.updateTaskNote(it.id, note) }
        }
    )

    BackupDialog(
        isOpen = isBackupOpen,
        onDismiss = { viewModel.openBackupDialog(false) },
        exportJsonString = exportJsonString,
        onImport = { json ->
            viewModel.importBackupDataString(json) { }
        },
        onReset = { viewModel.resetAllData() }
    )
}
