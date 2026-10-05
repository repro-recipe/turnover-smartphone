package com.example.turnover.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turnover.model.Task
import com.example.turnover.ui.theme.DadsBlue
import com.example.turnover.ui.theme.DadsBlueBorder
import com.example.turnover.ui.theme.DadsBlueSubtle
import com.example.turnover.ui.theme.DadsBorder
import com.example.turnover.ui.theme.DadsBorderLight
import com.example.turnover.ui.theme.DadsError
import com.example.turnover.ui.theme.DadsSuccess
import com.example.turnover.ui.theme.DadsSuccessBg
import com.example.turnover.ui.theme.DadsTextMuted
import com.example.turnover.ui.theme.DadsTextPrimary
import com.example.turnover.ui.theme.DadsTextSecondary
import com.example.turnover.ui.theme.DadsWarning

@Composable
fun TaskCard(
    task: Task,
    onToggleTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onOpenNoteDialog: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val isImprovement = task.isImprovementAction

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isImprovement) DadsBlueSubtle.copy(alpha = 0.6f) else Color.White
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = if (isImprovement) 1.5.dp else 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isImprovement) DadsBlueBorder else DadsBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isImprovement) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DadsBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "改善",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "改善アクション (+1pt)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = DadsTextSecondary
                        )
                    }

                    // Priority
                    if (task.priority == "high") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "最優先",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsError
                            )
                        }
                    }
                }

                // Estimated minutes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "目安時間",
                        tint = DadsTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${task.estimatedMinutes}分",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DadsTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onToggleTask(task.id) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isImprovement) DadsBlue else DadsSuccess,
                        uncheckedColor = DadsBorder
                    ),
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .testTag("task_checkbox_${task.id}")
                )

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.completed) DadsTextSecondary else DadsTextPrimary,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            fontSize = 12.sp,
                            color = DadsTextSecondary
                        )
                    }

                    // Target Failure Context (if improvement)
                    if (!task.targetFailureDescription.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .border(1.dp, DadsBlueBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "克服対象: ${task.targetFailureDescription}",
                                fontSize = 11.sp,
                                color = DadsBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // User note (Reflection memo)
                    if (!task.userNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEF9C3))
                                .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ModeComment,
                                contentDescription = "メモ",
                                tint = Color(0xFF854D0E),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = task.userNote,
                                fontSize = 11.sp,
                                color = Color(0xFF713F12)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onOpenNoteDialog(task) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "メモを編集",
                            tint = if (task.userNote.isNullOrBlank()) DadsTextMuted else DadsBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDeleteTask(task.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "タスク削除",
                            tint = DadsTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
