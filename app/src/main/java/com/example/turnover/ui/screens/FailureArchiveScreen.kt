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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircle2
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.turnover.model.FailureItem
import com.example.turnover.ui.components.AddFailureDialog
import com.example.turnover.ui.components.AppHeader
import com.example.turnover.ui.components.BackupDialog
import com.example.turnover.ui.components.MarkOvercomeDialog
import com.example.turnover.ui.theme.DadsBgCanvas
import com.example.turnover.ui.theme.DadsBlue
import com.example.turnover.ui.theme.DadsBlueSubtle
import com.example.turnover.ui.theme.DadsBorder
import com.example.turnover.ui.theme.DadsBorderLight
import com.example.turnover.ui.theme.DadsError
import com.example.turnover.ui.theme.DadsSuccess
import com.example.turnover.ui.theme.DadsSuccessBg
import com.example.turnover.ui.theme.DadsTextMuted
import com.example.turnover.ui.theme.DadsTextPrimary
import com.example.turnover.ui.theme.DadsTextSecondary
import com.example.turnover.ui.viewmodel.AppScreen
import com.example.turnover.ui.viewmodel.TurnOverViewModel
import kotlinx.coroutines.launch

@Composable
fun FailureArchiveScreen(
    viewModel: TurnOverViewModel,
    modifier: Modifier = Modifier
) {
    val failures by viewModel.failures.collectAsStateWithLifecycle()
    val isBackupOpen by viewModel.isBackupDialogOpen.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var filter by remember { mutableStateOf("all") } // "all", "overcome", "in_action", "unresolved"
    var searchQuery by remember { mutableStateOf("") }
    var isAddFailureOpen by remember { mutableStateOf(false) }
    var failureToOvercome by remember { mutableStateOf<FailureItem?>(null) }
    var exportJsonString by remember { mutableStateOf("") }

    val overcomeCount = failures.count { it.status == "overcome" }
    val inActionCount = failures.count { it.status == "in_action" }
    val unresolvedCount = failures.count { it.status == "unresolved" }

    val filteredFailures = failures.filter { f ->
        val matchesFilter = when (filter) {
            "overcome" -> f.status == "overcome"
            "in_action" -> f.status == "in_action"
            "unresolved" -> f.status == "unresolved"
            else -> true
        }

        val q = searchQuery.trim().lowercase()
        val matchesSearch = if (q.isBlank()) true else {
            f.failure.lowercase().contains(q) ||
                    f.rootCause.lowercase().contains(q) ||
                    f.context.lowercase().contains(q)
        }

        matchesFilter && matchesSearch
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppHeader(
                currentScreen = AppScreen.ARCHIVE,
                onNavigateHome = { viewModel.navigateTo(AppScreen.HOME) },
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
            // Back navigation link
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.HOME) }
                        .padding(vertical = 4.dp)
                        .testTag("archive_back_home_link")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "戻る",
                        tint = DadsBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ホーム（今日やること）に戻る",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DadsBlue
                    )
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "つまずき・克服の記録 (合計 ${failures.size}件)",
                                    fontSize = 11.sp,
                                    color = DadsBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "できるようになったこと（克服ノート）",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DadsTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "できなかった問題やつまずきを記録し、改善アクションを実行して克服できたら「できるようになった！」を記録します。",
                                    fontSize = 12.sp,
                                    color = DadsTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { isAddFailureOpen = true },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("archive_add_failure_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "追加",
                                    tint = DadsBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("つまずき追加", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DadsBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3 Summary Columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Overcome
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .border(1.dp, DadsBorderLight, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = "解決できた（克服）", fontSize = 10.sp, color = DadsTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle2,
                                        contentDescription = null,
                                        tint = DadsSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$overcomeCount 件",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DadsSuccess
                                    )
                                }
                            }

                            // In Action
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .border(1.dp, DadsBorderLight, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = "いま対策中", fontSize = 10.sp, color = DadsTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = DadsBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$inActionCount 件",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DadsBlue
                                    )
                                }
                            }

                            // Unresolved
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .border(1.dp, DadsBorderLight, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(text = "これから対策", fontSize = 10.sp, color = DadsTextSecondary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = DadsTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$unresolvedCount 件",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DadsTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("キーワード検索（問題、原因、教科...）") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = DadsTextMuted)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "all" to "すべて (${failures.size})",
                            "overcome" to "克服済 (${overcomeCount})",
                            "in_action" to "対策中 (${inActionCount})",
                            "unresolved" to "未対策 (${unresolvedCount})"
                        ).forEach { (key, label) ->
                            val isSelected = filter == key
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DadsBlue else Color.White)
                                    .border(
                                        1.dp,
                                        if (isSelected) DadsBlue else DadsBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { filter = key }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("archive_filter_$key")
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

            // Failure items list
            if (filteredFailures.isEmpty()) {
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
                        Text(
                            text = if (failures.isEmpty()) {
                                "登録されたつまずき記録はありません。\n夜のふりかえりを行うと、AIがつまずきを自動で抽出・登録します！"
                            } else {
                                "該当するつまずき記録は見つかりませんでした。"
                            },
                            fontSize = 12.sp,
                            color = DadsTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredFailures, key = { it.id }) { item ->
                    val isOvercome = item.status == "overcome"
                    val isInAction = item.status == "in_action"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOvercome) DadsSuccessBg.copy(alpha = 0.5f) else Color.White
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (isOvercome) Color(0xFF86EFAC) else DadsBorder
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Row 1: Badges & Date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when {
                                                    isOvercome -> DadsSuccess
                                                    isInAction -> DadsBlue
                                                    else -> Color(0xFF6B7280)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isOvercome -> "克服完了"
                                                isInAction -> "対策中"
                                                else -> "未対策"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Context Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFF3F4F6))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.context,
                                            fontSize = 10.sp,
                                            color = DadsTextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = item.date,
                                    fontSize = 11.sp,
                                    color = DadsTextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Failure Content
                            Text(
                                text = item.failure,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsTextPrimary
                            )

                            if (item.rootCause.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "原因分析: ${item.rootCause}",
                                    fontSize = 12.sp,
                                    color = DadsTextSecondary
                                )
                            }

                            // Overcome details or Action button
                            if (isOvercome) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = DadsSuccess,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "克服達成 (${item.overcomeDate ?: ""})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DadsSuccess
                                            )
                                        }
                                        if (!item.overcomeReflection.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = item.overcomeReflection,
                                                fontSize = 12.sp,
                                                color = DadsTextPrimary
                                            )
                                        }
                                        if (!item.praiseNote.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "褒め言葉: ${item.praiseNote}",
                                                fontSize = 11.sp,
                                                color = DadsSuccess,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { failureToOvercome = item },
                                    colors = ButtonDefaults.buttonColors(containerColor = DadsSuccess),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("mark_overcome_btn_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "できるようになった！ (+1pt獲得)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Dialogs
    AddFailureDialog(
        isOpen = isAddFailureOpen,
        onDismiss = { isAddFailureOpen = false },
        onAdd = { failure, context, rootCause, severity ->
            viewModel.addFailure(failure, context, rootCause, severity)
        }
    )

    MarkOvercomeDialog(
        isOpen = failureToOvercome != null,
        failureText = failureToOvercome?.failure ?: "",
        onDismiss = { failureToOvercome = null },
        onConfirm = { reflection ->
            failureToOvercome?.let {
                viewModel.markFailureOvercome(it.id, reflection)
            }
            failureToOvercome = null
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
