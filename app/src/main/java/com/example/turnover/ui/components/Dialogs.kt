package com.example.turnover.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.turnover.model.DailyRoutine
import com.example.turnover.model.Task
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

@Composable
fun AddEditTaskDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onAddTask: (title: String, desc: String, isImprovement: Boolean, targetFailure: String?, minutes: Int) -> Unit
) {
    if (!isOpen) return

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("25") }
    var isImprovement by remember { mutableStateOf(false) }
    var targetFailure by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "新しいタスクの追加", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("タスク名（例: 数学のワーク3ページ解く）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("詳細（任意）") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter { c -> c.isDigit() } },
                    label = { Text("目安所要時間（分）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Improvement Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isImprovement) DadsBlueSubtle else Color(0xFFF3F4F6))
                        .clickable { isImprovement = !isImprovement }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isImprovement,
                        onCheckedChange = { isImprovement = it },
                        colors = CheckboxDefaults.colors(checkedColor = DadsBlue)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "つまずきの改善アクションにする (+1pt)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isImprovement) DadsBlue else DadsTextPrimary
                        )
                        Text(
                            text = "前日の失敗や課題を克服するためのタスク",
                            fontSize = 10.sp,
                            color = DadsTextSecondary
                        )
                    }
                }

                if (isImprovement) {
                    OutlinedTextField(
                        value = targetFailure,
                        onValueChange = { targetFailure = it },
                        label = { Text("克服したい失敗・つまずき") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val min = minutes.toIntOrNull() ?: 25
                        onAddTask(title, desc, isImprovement, if (isImprovement) targetFailure else null, min)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DadsBlue)
            ) {
                Text("追加する", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = DadsTextSecondary)
            }
        }
    )
}

@Composable
fun AddEditHabitDialog(
    isOpen: Boolean,
    routineToEdit: DailyRoutine? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String?) -> Unit
) {
    if (!isOpen) return

    var title by remember(routineToEdit) { mutableStateOf(routineToEdit?.title ?: "") }
    var desc by remember(routineToEdit) { mutableStateOf(routineToEdit?.description ?: "") }

    val presetSuggestions = listOf(
        "英単語・用語の暗記（10分）" to "朝やスキマ時間に単語帳を見返す",
        "教科書・参考書の音読（15分）" to "声に出して重要語句を定着させる",
        "計算ドリル・基礎問（10分）" to "毎日継続して精度と速度を保つ",
        "明日の学習計画・準備（5分）" to "就寝前に翌日の予定と持ち物を確認"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (routineToEdit != null) "学習習慣の編集" else "毎日の学習習慣を追加",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("習慣タイトル（例: 英単語暗記 10分）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("説明・コツ（任意）") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (routineToEdit == null) {
                    Text(
                        text = "定番の学習習慣プリセット:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DadsTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    presetSuggestions.forEach { (pTitle, pDesc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF9FAFB))
                                .border(1.dp, DadsBorderLight, RoundedCornerShape(6.dp))
                                .clickable {
                                    title = pTitle
                                    desc = pDesc
                                }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(text = pTitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DadsBlue)
                                Text(text = pDesc, fontSize = 10.sp, color = DadsTextMuted)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, desc.ifBlank { null })
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DadsBlue)
            ) {
                Text(if (routineToEdit != null) "保存する" else "登録する", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = DadsTextSecondary)
            }
        }
    )
}

@Composable
fun EditNoteDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    if (task == null) return

    var note by remember(task) { mutableStateOf(task.userNote ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "タスクのひとことメモ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "対象: ${task.title}",
                    fontSize = 12.sp,
                    color = DadsTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("気づき・つまずき・成果メモ") },
                    placeholder = { Text("例: 3問目でケアレスミスした、集中できた 等") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text(
                    text = "※ ここで記録したメモは夜のふりかえりAIに自動で反映されます。",
                    fontSize = 11.sp,
                    color = DadsTextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveNote(note)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DadsBlue)
            ) {
                Text("メモを保存", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = DadsTextSecondary)
            }
        }
    )
}

@Composable
fun MarkOvercomeDialog(
    isOpen: Boolean,
    failureText: String,
    onDismiss: () -> Unit,
    onConfirm: (reflection: String) -> Unit
) {
    if (!isOpen) return

    var reflection by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = DadsSuccess,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "できるようになった！を記録", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DadsSuccessBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "克服したつまずき:",
                            fontSize = 11.sp,
                            color = DadsSuccess,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = failureText,
                            fontSize = 13.sp,
                            color = DadsTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = { Text("克服のコツ・できるようになった理由") },
                    placeholder = { Text("例: 解き方の公式を手順表にまとめたら迷わず解けるようになった！") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Text(
                    text = "克服を記録すると、改善ポイントが +1pt 獲得されます！",
                    fontSize = 11.sp,
                    color = DadsSuccess,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reflection.ifBlank { "改善アクションを実行し克服完了！" })
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DadsSuccess)
            ) {
                Text("克服を確定 (+1pt)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = DadsTextSecondary)
            }
        }
    )
}

@Composable
fun AddFailureDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onAdd: (failure: String, context: String, rootCause: String, severity: String) -> Unit
) {
    if (!isOpen) return

    var failure by remember { mutableStateOf("") }
    var context by remember { mutableStateOf("数学") }
    var rootCause by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("moderate") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "新しいつまずき・課題を手動登録", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = failure,
                    onValueChange = { failure = it },
                    label = { Text("できなかったこと・つまずき") },
                    placeholder = { Text("例: 二次関数のグラフの頂点座標が求められなかった") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = context,
                    onValueChange = { context = it },
                    label = { Text("教科・コンテクスト") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rootCause,
                    onValueChange = { rootCause = it },
                    label = { Text("原因分析（任意）") },
                    placeholder = { Text("例: 平方完成の計算手順の理解不足") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (failure.isNotBlank()) {
                        onAdd(failure, context, rootCause, severity)
                        onDismiss()
                    }
                },
                enabled = failure.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DadsBlue)
            ) {
                Text("登録する", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = DadsTextSecondary)
            }
        }
    )
}

@Composable
fun BackupDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    exportJsonString: String,
    onImport: (String) -> Unit,
    onReset: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Export, 1: Import
    var importText by remember { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "データ保存・インポート",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DadsTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "閉じる")
                    }
                }

                // Tab Switcher
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { selectedTab = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) DadsBlue else Color(0xFFF3F4F6),
                            contentColor = if (selectedTab == 0) Color.White else DadsTextSecondary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("エクスポート", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { selectedTab = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) DadsBlue else Color(0xFFF3F4F6),
                            contentColor = if (selectedTab == 1) Color.White else DadsTextSecondary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("インポート", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (selectedTab == 0) {
                    // Export View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DadsBlueSubtle)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "現在の学習データ（タスク・習慣・克服ノート・ストリーク）をJSON形式の文字列で出力しました。コピーして保存してください。",
                            fontSize = 11.sp,
                            color = DadsBlue,
                            lineHeight = 15.sp
                        )
                    }

                    OutlinedTextField(
                        value = exportJsonString,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("TurnOver Backup", exportJsonString)
                            clipboard.setPrimaryClip(clip)
                            copied = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (copied) "コピーしました！" else "文字列をクリップボードにコピー", color = Color.White, fontSize = 12.sp)
                    }
                } else {
                    // Import View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFFBEB))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "バックアップしたJSON文字列を貼り付けると、データを復元できます。現在のデータは上書きされます。",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 15.sp
                        )
                    }

                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("JSONバックアップ文字列をここに貼り付け...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )

                    Button(
                        onClick = {
                            if (importText.isNotBlank()) {
                                onImport(importText.trim())
                                onDismiss()
                            }
                        },
                        enabled = importText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("インポートを実行", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Reset option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        onReset()
                        onDismiss()
                    }) {
                        Text("全データを白紙に初期化", color = DadsError, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
