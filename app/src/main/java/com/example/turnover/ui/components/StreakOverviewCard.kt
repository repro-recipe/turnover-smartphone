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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.turnover.model.StreakData
import com.example.turnover.model.UserStats
import com.example.turnover.ui.theme.DadsBlue
import com.example.turnover.ui.theme.DadsBlueSubtle
import com.example.turnover.ui.theme.DadsBorder
import com.example.turnover.ui.theme.DadsBorderLight
import com.example.turnover.ui.theme.DadsSuccess
import com.example.turnover.ui.theme.DadsSuccessBg
import com.example.turnover.ui.theme.DadsTextMuted
import com.example.turnover.ui.theme.DadsTextPrimary
import com.example.turnover.ui.theme.DadsTextSecondary
import com.example.turnover.ui.theme.DadsWarning

@Composable
fun StreakOverviewCard(
    streak: StreakData,
    stats: UserStats,
    onStartReflection: () => Unit,
    onNavigateArchive: () => Unit,
    onAdvanceToNextDayDirect: () => Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(DadsBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Streak Header & Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "ストリーク",
                            tint = Color(0xFFEA580C),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "継続 ",
                                fontSize = 13.sp,
                                color = DadsTextSecondary
                            )
                            Text(
                                text = "${streak.currentStreak}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsTextPrimary
                            )
                            Text(
                                text = " 日目",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsTextPrimary
                            )
                        }
                        Text(
                            text = "最高連続: ${streak.bestStreak}日",
                            fontSize = 11.sp,
                            color = DadsTextMuted
                        )
                    }
                }

                // Stats Badges (Improvement points & overcome count)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Points Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "ポイント",
                                tint = DadsWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (stats.improvementPoints % 1.0 == 0.0) stats.improvementPoints.toInt().toString() else stats.improvementPoints} pt",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    // Overcome count
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DadsSuccessBg)
                            .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(8.dp))
                            .clickable { onNavigateArchive() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("streak_card_overcome_badge")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "克服",
                                tint = DadsSuccess,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "克服 ${stats.overcomeFailuresCount}件",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DadsSuccess
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7-day weekly history row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, DadsBorderLight, RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                streak.weeklyHistory.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Text(
                            text = day.dayName,
                            fontSize = 11.sp,
                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (day.isToday) DadsBlue else DadsTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        day.completed -> DadsSuccess
                                        day.isToday -> DadsBlueSubtle
                                        else -> Color(0xFFE5E7EB)
                                    }
                                )
                                .border(
                                    width = if (day.isToday) 2.dp else 1.dp,
                                    color = if (day.isToday) DadsBlue else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.completed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "完了",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (day.isToday) {
                                Text(
                                    text = "今日",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DadsBlue
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Evening Reflection & Direct Advance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartReflection,
                    colors = ButtonDefaults.buttonColors(containerColor = DadsBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("start_evening_reflection_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = "夜のふりかえり",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "夜のふりかえりを始める",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onAdvanceToNextDayDirect,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("direct_next_day_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "翌日に進む",
                        tint = DadsBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "翌日へ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DadsBlue
                    )
                }
            }
        }
    }
}
