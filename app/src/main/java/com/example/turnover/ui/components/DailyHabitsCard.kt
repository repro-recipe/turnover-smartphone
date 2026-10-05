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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turnover.model.DailyRoutine
import com.example.turnover.ui.theme.DadsBlue
import com.example.turnover.ui.theme.DadsBlueSubtle
import com.example.turnover.ui.theme.DadsBorder
import com.example.turnover.ui.theme.DadsBorderLight
import com.example.turnover.ui.theme.DadsSuccess
import com.example.turnover.ui.theme.DadsSuccessBg
import com.example.turnover.ui.theme.DadsTextMuted
import com.example.turnover.ui.theme.DadsTextPrimary
import com.example.turnover.ui.theme.DadsTextSecondary

@Composable
fun DailyHabitsCard(
    routines: List<DailyRoutine>,
    onToggleRoutine: (String) -> Unit,
    onOpenAddRoutine: () -> Unit,
    onEditRoutine: (DailyRoutine) -> Unit,
    onDeleteRoutine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DadsBlueSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "デイリー習慣",
                            tint = DadsBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "毎日の学習習慣",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DadsTextPrimary
                        )
                        Text(
                            text = "3日以上継続で毎日+0.5pt獲得",
                            fontSize = 11.sp,
                            color = DadsTextSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenAddRoutine,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("add_routine_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "習慣を追加",
                        tint = DadsBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "習慣を追加",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DadsBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (routines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, DadsBorderLight, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "登録された習慣はありません。「習慣を追加」から日課を登録しましょう！",
                        fontSize = 12.sp,
                        color = DadsTextMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    routines.forEach { routine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (routine.completedToday) DadsSuccessBg.copy(alpha = 0.5f) else Color(0xFFFAFAFA))
                                .border(
                                    width = 1.dp,
                                    color = if (routine.completedToday) Color(0xFF86EFAC) else DadsBorderLight,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = routine.completedToday,
                                onCheckedChange = { onToggleRoutine(routine.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DadsSuccess,
                                    uncheckedColor = DadsBorder
                                ),
                                modifier = Modifier.testTag("routine_checkbox_${routine.id}")
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = routine.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (routine.completedToday) DadsTextSecondary else DadsTextPrimary,
                                    textDecoration = if (routine.completedToday) TextDecoration.LineThrough else TextDecoration.None
                                )

                                if (!routine.description.isNullOrBlank()) {
                                    Text(
                                        text = routine.description,
                                        fontSize = 11.sp,
                                        color = DadsTextMuted
                                    )
                                }

                                // Streak Status Badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "継続",
                                        tint = if (routine.streakCount >= 3) Color(0xFFEA580C) else DadsTextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (routine.streakCount >= 3) {
                                            "${routine.streakCount}日連続達成 (+0.5pt/日)"
                                        } else {
                                            "現在${routine.streakCount}日連続 (あと${3 - routine.streakCount}日で+0.5pt)"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = if (routine.streakCount >= 3) FontWeight.Bold else FontWeight.Normal,
                                        color = if (routine.streakCount >= 3) Color(0xFFEA580C) else DadsTextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onEditRoutine(routine) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "編集",
                                        tint = DadsTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteRoutine(routine.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "削除",
                                        tint = DadsTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
