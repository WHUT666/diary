package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DiaryEntry
import com.example.security.SecurityLockManager
import com.example.ui.DiaryStats
import com.example.ui.components.IosColors
import com.example.ui.components.IosGroupedCard
import com.example.ui.components.IosIconTile
import com.example.ui.components.IosInsetDivider
import com.example.ui.components.IosNavigationRow
import com.example.ui.components.IosSectionHeader
import com.example.ui.theme.AppThemeManager
import com.example.util.HapticFeedbackUtil

/**
 * 个人空间与设置中心 (Space & Settings Screen)
 * 采用原生 Apple iOS Settings 经典 Inset Grouped 风格与视觉规范。
 */
@Composable
fun SettingsScreen(
    entries: List<DiaryEntry>,
    stats: DiaryStats,
    themeManager: AppThemeManager,
    securityLockManager: SecurityLockManager,
    onOpenThemeSelector: () -> Unit,
    onOpenSecuritySettings: () -> Unit,
    onOpenReminderDialog: () -> Unit,
    onOpenBackupDialog: () -> Unit,
    onOpenBookExportDialog: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLockEnabled = securityLockManager.isLockEnabled
    val currentThemePreset = themeManager.currentTheme.collectAsState().value
    val currentTheme = currentThemePreset.name

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Apple Large Title Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "空间与设置",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "个性化偏好、数据资产守护与隐私安全",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = IosColors.SystemGray,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        // 1. 写作数据全景卡片 (Apple Activity / Fitness Card Style)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        HapticFeedbackUtil.lightImpact(context)
                        onOpenStats()
                    }
                    .testTag("settings_stats_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IosIconTile(
                                icon = Icons.Default.Insights,
                                backgroundColor = IosColors.SystemOrange,
                                size = 32.dp,
                                iconSize = 18.dp
                            )
                            Column {
                                Text(
                                    text = "写作足迹与成就",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "记录心灵成长的每一次脉动",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = IosColors.SystemGray
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "图表分析",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = IosColors.SystemGray3,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SettingsStatItem(label = "连续打卡", value = "${stats.streakDays} 天", color = IosColors.SystemOrange)
                        SettingsStatItem(label = "累积篇数", value = "${entries.size} 篇", color = IosColors.SystemBlue)
                        SettingsStatItem(label = "书写字数", value = "${stats.totalWords} 字", color = IosColors.SystemGreen)
                        SettingsStatItem(label = "珍藏瞬间", value = "${entries.count { it.isFavorite }} 篇", color = IosColors.SystemPink)
                    }
                }
            }
        }

        // 2. 外观与习惯
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IosSectionHeader(title = "外观与习惯")
                IosGroupedCard {
                    IosNavigationRow(
                        title = "主题外观风格",
                        subtitle = "当前：$currentTheme",
                        icon = Icons.Default.Palette,
                        iconBackgroundColor = IosColors.SystemIndigo,
                        onClick = onOpenThemeSelector,
                        testTag = "settings_item_theme"
                    )
                    IosInsetDivider()
                    IosNavigationRow(
                        title = "每日写作温情提醒",
                        subtitle = "定时温情寄语，陪伴每一天",
                        icon = Icons.Default.Notifications,
                        iconBackgroundColor = IosColors.SystemOrange,
                        onClick = onOpenReminderDialog,
                        testTag = "settings_item_reminder"
                    )
                }
            }
        }

        // 3. 隐私与安全
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IosSectionHeader(title = "隐私与守护")
                IosGroupedCard {
                    IosNavigationRow(
                        title = "隐私安全锁",
                        subtitle = if (isLockEnabled) "已开启生物识别与PIN保护" else "未开启保护，日记直接可见",
                        icon = Icons.Default.Security,
                        iconBackgroundColor = IosColors.SystemGreen,
                        trailingText = if (isLockEnabled) "已保护" else "未开启",
                        onClick = onOpenSecuritySettings,
                        testTag = "settings_item_security"
                    )
                }
            }
        }

        // 4. 数据资产与归档
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IosSectionHeader(title = "数据资产与归档")
                IosGroupedCard {
                    IosNavigationRow(
                        title = "数据本地备份与恢复",
                        subtitle = "离线冷备份与全量 JSON 导出恢复",
                        icon = Icons.Default.CloudSync,
                        iconBackgroundColor = IosColors.SystemTeal,
                        onClick = onOpenBackupDialog,
                        testTag = "settings_item_backup"
                    )
                    IosInsetDivider()
                    IosNavigationRow(
                        title = "导出日记电子书册",
                        subtitle = "汇编为 Markdown / TXT 永久留存",
                        icon = Icons.Default.Info,
                        iconBackgroundColor = IosColors.SystemBlue,
                        onClick = onOpenBookExportDialog,
                        testTag = "settings_item_book_export"
                    )
                }
            }
        }

        // 5. 智能心流引擎
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IosSectionHeader(title = "智能心流引擎")
                IosGroupedCard {
                    IosNavigationRow(
                        title = "AI 模型与密钥配置",
                        subtitle = "Gemini 2.5 Flash / Pro 及端点自定义",
                        icon = Icons.Default.SmartToy,
                        iconBackgroundColor = IosColors.SystemPurple,
                        onClick = onOpenAiSettings,
                        testTag = "settings_item_ai"
                    )
                }
            }
        }

        // 底部版本与关于 (Apple Footer Caption)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "日记 · 心灵与时光的港湾",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = IosColors.SystemGray,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "端侧离线存储 · 数据完全归属于你",
                    style = TextStyle(
                        fontSize = 11.5.sp,
                        color = IosColors.SystemGray2,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingsStatItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 11.5.sp,
                color = IosColors.SystemGray,
                fontWeight = FontWeight.Normal
            )
        )
    }
}
