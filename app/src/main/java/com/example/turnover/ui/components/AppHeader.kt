package com.example.turnover.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.turnover.ui.theme.DadsBlue
import com.example.turnover.ui.theme.DadsBlueSubtle
import com.example.turnover.ui.theme.DadsBorder
import com.example.turnover.ui.theme.DadsTextPrimary
import com.example.turnover.ui.theme.DadsTextSecondary
import com.example.turnover.ui.viewmodel.AppScreen

@Composable
fun AppHeader(
    currentScreen: AppScreen,
    onNavigateHome: () => Unit,
    onOpenBackup: () => Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
            // Brand Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateHome() }
                    .padding(4.dp)
                    .testTag("header_brand_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DadsBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "TurnOver ロゴ",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TurnOver",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DadsTextPrimary,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "学習改善ノート",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DadsTextSecondary,
                        lineHeight = 13.sp
                    )
                }
            }

            // Right utility actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentScreen == AppScreen.ARCHIVE) {
                    OutlinedButton(
                        onClick = onNavigateHome,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("header_back_to_home_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る",
                            tint = DadsBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ホーム",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DadsBlue
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenBackup,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("header_backup_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "データ保存・復元",
                        tint = DadsBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "保存/復元",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DadsBlue
                    )
                }
            }
        }
    }
}
