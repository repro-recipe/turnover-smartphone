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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.turnover.model.Task
import com.example.turnover.ui.theme.DadsBgCanvas
import com.example.turnover.ui.theme.DadsBlue
import com.example.turnover.ui.theme.DadsBlueSubtle
import com.example.turnover.ui.theme.DadsBorder
import com.example.turnover.ui.theme.DadsBorderLight
import com.example.turnover.ui.theme.DadsError
import com.example.turnover.ui.theme.DadsErrorBg
import com.example.turnover.ui.theme.DadsSuccess
import com.example.turnover.ui.theme.DadsSuccessBg
import com.example.turnover.ui.theme.DadsTextMuted
import com.example.turnover.ui.theme.DadsTextPrimary
import com.example.turnover.ui.theme.DadsTextSecondary
import com.example.turnover.ui.viewmodel.AppScreen
import com.example.turnover.ui.viewmodel.TurnOverViewModel

@Composable
fun EveningReflectionScreen(
    viewModel: TurnOverViewModel,
    modifier: Modifier = Modifier
) {
    val step by viewModel.reflectionStep.collectAsStateWithLifecycle()
    val journalText by viewModel.journalText.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisResult by viewModel.analysisResult.collectAsStateWithLifecycle()
    val selectedImprovements by viewModel.selectedImprovements.collectAsStateWithLifecycle()
    val confirmedTasks by viewModel.confirmedNextDayTasks.collectAsStateWithLifecycle()
    val currentTasks by viewModel.tasks.collectAsStateWithLifecycle()

    var customActionTitle by remember { mutableStateOf("") }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskMinutes by remember { mutableStateOf("25") }
    var newTaskIsImprovement by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (step > 1) viewModel.setReflectionStep(step - 1)
                                else viewModel.navigateTo(AppScreen.HOME)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = DadsBlue)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "夜のふりかえりノート",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsTextPrimary
                            )
                            Text(
                                text = "ステップ $step / 4: ${
                                    when (step) {
                                        1 -> "振り返りを記録"
                                        2 -> "AI分析の確認"
                                        3 -> "明日の行動計画"
                                        else -> "明日のタスク確認"
                                    }
                                }",
                                fontSize = 11.sp,
                                color = DadsBlue
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる", tint = DadsTextSecondary)
                    }
                }
            }
        },
        containerColor = DadsBgCanvas
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Step Indicator Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, DadsBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("① 振り返り", "② 分析", "③ 計画", "④ 確認").forEachIndexed { idx, title ->
                        val stepNum = idx + 1
                        val isActive = step == stepNum
                        val isDone = step > stepNum

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> DadsSuccess
                                            isActive -> DadsBlue
                                            else -> Color(0xFFE5E7EB)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                } else {
                                    Text(text = "$stepNum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isActive) Color.White else DadsTextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontSize = 10.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) DadsBlue else DadsTextSecondary
                            )
                        }
                    }
                }
            }

            // Step Content
            when (step) {
                1 -> {
                    // STEP 1: 振り返り記録
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "今日の学習を自由に振り返る",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DadsTextPrimary
                                )
                                Text(
                                    text = "今日できたこと、間違えたこと・つまずいた問題、明日は何がしたいかを自由に書き出してください。AIが自動でつまずきを抽出し、明日の改善アクションに変換します。",
                                    fontSize = 12.sp,
                                    color = DadsTextSecondary,
                                    lineHeight = 16.sp
                                )

                                // Current Tasks reference
                                if (currentTasks.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFAFAFA))
                                            .border(1.dp, DadsBorderLight, RoundedCornerShape(6.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(text = "【今日のタスク状況】", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DadsTextSecondary)
                                            currentTasks.forEach { t ->
                                                Text(
                                                    text = "・ ${if (t.completed) "✓" else "□"} ${t.title} ${if (!t.userNote.isNullOrBlank()) "（メモ: ${t.userNote}）" else ""}",
                                                    fontSize = 11.sp,
                                                    color = if (t.completed) DadsSuccess else DadsTextPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = journalText,
                                    onValueChange = { viewModel.setJournalText(it) },
                                    label = { Text("今日のふりかえりノート") },
                                    placeholder = {
                                        Text("例:\n・数学ワークを3ページ解いた。しかし関数の応用問題で5回計算ミスをして解けなかった。\n・英単語はスムーズに20個覚えられた。\n・明日は数学の計算ミスの見直しと理科の復習をしたい。")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    maxLines = 10
                                )

                                Button(
                                    onClick = { viewModel.analyzeReflection() },
                                    enabled = journalText.isNotBlank() && !isAnalyzing,
                                    colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("reflection_analyze_btn")
                                ) {
                                    if (isAnalyzing) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AIが分析中...", color = Color.White)
                                    } else {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AIで分析して計画を立てる", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // STEP 2: 分析結果
                    val result = analysisResult
                    if (result != null) {
                        item {
                            // Encouragement note
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DadsBlueSubtle)
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = DadsBlue, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "AIコーチからのメッセージ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DadsBlue)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = result.encouragementNote, fontSize = 13.sp, color = DadsTextPrimary, lineHeight = 17.sp)
                                    }
                                }
                            }
                        }

                        // Achievements
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DadsSuccess, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "成功した点・できたこと", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DadsSuccess)
                                    }
                                    result.extractedAchievements.forEach { ach ->
                                        Text(text = "・ $ach", fontSize = 12.sp, color = DadsTextPrimary)
                                    }
                                }
                            }
                        }

                        // Failures
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = DadsError, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "つまずき・改善したい点", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DadsError)
                                    }
                                    result.extractedFailures.forEach { f ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DadsErrorBg)
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Text(text = f.failure, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DadsError)
                                                Text(text = "根本原因: ${f.rootCause}", fontSize = 11.sp, color = DadsTextSecondary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Tomorrow Plans
                        if (result.extractedTomorrowPlans.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "明日の予定・やりたいこと", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DadsTextPrimary)
                                        result.extractedTomorrowPlans.forEach { p ->
                                            Text(text = "・ $p", fontSize = 12.sp, color = DadsTextPrimary)
                                        }
                                    }
                                }
                            }
                        }

                        // Next button
                        item {
                            Button(
                                onClick = { viewModel.setReflectionStep(3) },
                                colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("reflection_goto_plan_btn")
                            ) {
                                Text("次へ：明日の計画を立てる", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                3 -> {
                    // STEP 3: 改善行動の選択
                    item {
                        Text(
                            text = "明日の改善アクション（スモールステップ）を選択",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DadsTextPrimary
                        )
                    }

                    items(selectedImprovements, key = { it.id }) { imp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (imp.selected) DadsBlueSubtle else Color.White)
                                .border(
                                    1.dp,
                                    if (imp.selected) Color(0xFFBFDBFE) else DadsBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.toggleImprovementSelection(imp.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Checkbox(
                                checked = imp.selected,
                                onCheckedChange = { viewModel.toggleImprovementSelection(imp.id) },
                                colors = CheckboxDefaults.colors(checkedColor = DadsBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = imp.actionTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (imp.selected) DadsBlue else DadsTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = imp.actionDetail,
                                    fontSize = 12.sp,
                                    color = DadsTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "目安時間: ${imp.estimatedMinutes}分 / 対象: ${imp.targetFailure}",
                                    fontSize = 11.sp,
                                    color = DadsTextMuted
                                )
                            }
                        }
                    }

                    // Add Custom action
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customActionTitle,
                                    onValueChange = { customActionTitle = it },
                                    placeholder = { Text("自分で対策を追加...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.addCustomImprovement(customActionTitle)
                                        customActionTitle = ""
                                    },
                                    enabled = customActionTitle.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("追加")
                                }
                            }
                        }
                    }

                    // Next to Confirmation
                    item {
                        Button(
                            onClick = { viewModel.prepareConfirmedTasks() },
                            colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("reflection_goto_confirm_btn")
                        ) {
                            Text("次へ：明日のタスクを確認", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                4 -> {
                    // STEP 4: 明日のタスク確認 & 確定
                    item {
                        Column {
                            Text(
                                text = "明日のタスクリストの確認",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsTextPrimary
                            )
                            Text(
                                text = "明日朝起きてすぐできるように、タイトルや所要時間を最終確認してください。",
                                fontSize = 12.sp,
                                color = DadsTextSecondary
                            )
                        }
                    }

                    items(confirmedTasks, key = { it.id }) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (task.isImprovementAction) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(DadsBlue)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("改善 (+1pt)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(text = "${task.estimatedMinutes}分", fontSize = 11.sp, color = DadsTextMuted)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = task.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DadsTextPrimary)
                                    if (task.description.isNotBlank()) {
                                        Text(text = task.description, fontSize = 11.sp, color = DadsTextSecondary)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.removeConfirmedTask(task.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "削除", tint = DadsTextMuted)
                                }
                            }
                        }
                    }

                    // Add manual task to confirmed
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "タスクを追加する", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DadsTextSecondary)
                                OutlinedTextField(
                                    value = newTaskTitle,
                                    onValueChange = { newTaskTitle = it },
                                    placeholder = { Text("タスク名...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newTaskMinutes,
                                        onValueChange = { newTaskMinutes = it.filter { c -> c.isDigit() } },
                                        label = { Text("分") },
                                        singleLine = true,
                                        modifier = Modifier.width(80.dp)
                                    )

                                    Button(
                                        onClick = {
                                            val m = newTaskMinutes.toIntOrNull() ?: 20
                                            viewModel.addConfirmedTask(newTaskTitle, m, false)
                                            newTaskTitle = ""
                                        },
                                        enabled = newTaskTitle.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("追加")
                                    }
                                }
                            }
                        }
                    }

                    // Complete Reflection Button
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.finalizeReflection() },
                            colors = ButtonDefaults.buttonColors(containerColor = DadsSuccess),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("finalize_reflection_btn")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "夜のふりかえりを完了し、翌日の学習を開始する",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
