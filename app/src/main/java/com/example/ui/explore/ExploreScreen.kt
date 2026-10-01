package com.example.ui.explore

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DiaryEntry
import com.example.data.FlashbackManager
import com.example.ui.components.IosColors
import com.example.ui.components.IosIconTile
import com.example.ui.components.IosSectionHeader
import com.example.util.HapticFeedbackUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 探索与时光回忆中心 (Explore & Memories Space)
 * 聚合深度记忆、时光胶囊、心绪复盘、生活足迹、模板库与勋章成就殿堂。
 */
@Composable
fun ExploreScreen(
    entries: List<DiaryEntry>,
    streakDays: Int,
    onOpenFlashback: () -> Unit,
    onOpenTimeCapsule: () -> Unit,
    onOpenEmotionalReport: () -> Unit,
    onOpenFootprint: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenInspiration: () -> Unit,
    onOpenBadges: () -> Unit,
    onOpenBookExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val flashbacks = remember(entries) {
        FlashbackManager.findFlashbacks(entries)
    }

    val totalWords = remember(entries) {
        entries.sumOf { it.wordCount }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("explore_screen_list"),
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
                    text = "时光探索",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "漫步岁月长河，重温曾经心境，感受成长的足迹",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = IosColors.SystemGray,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        // 1. 焦点 Hero 卡片：那年今日 · 时光闪回 (Apple Memories Style)
        item {
            val hasFlashback = flashbacks.isNotEmpty()
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    0.5.dp,
                    if (hasFlashback) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        HapticFeedbackUtil.lightImpact(context)
                        onOpenFlashback()
                    }
                    .testTag("explore_card_flashback")
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
                                icon = Icons.Default.History,
                                backgroundColor = if (hasFlashback) IosColors.SystemIndigo else IosColors.SystemGray,
                                size = 32.dp,
                                iconSize = 18.dp
                            )
                            Column {
                                Text(
                                    text = "那年今日 · 时光闪回",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (hasFlashback) "发现 ${flashbacks.size} 篇往年同期的珍贵回忆" else "驻足片刻，与曾经的自己对话",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = IosColors.SystemGray
                                    )
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = IosColors.SystemGray3,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (hasFlashback) {
                        val firstMemory = flashbacks.first()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = firstMemory.relationTitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("·", color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = "${firstMemory.entry.mood} ${firstMemory.entry.moodLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = firstMemory.entry.content.take(70) + if (firstMemory.entry.content.length > 70) "…" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "今天暂未有往年同日日记。坚持每日记录，明年今日此时，岁月将为你呈上最动人的礼物。",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = IosColors.SystemGray,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // 2. 核心模块分栏一：时光与回忆
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IosSectionHeader(title = "时光印记")
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExploreGridCard(
                        title = "时光胶囊",
                        subtitle = "未来信笺与寄语",
                        emoji = "💌",
                        badge = "未来信",
                        iconBg = IosColors.SystemPink,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenTimeCapsule,
                        testTag = "explore_grid_capsule"
                    )

                    ExploreGridCard(
                        title = "心绪复盘",
                        subtitle = "情绪周报与心境",
                        emoji = "🌿",
                        badge = "AI复盘",
                        iconBg = IosColors.SystemGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenEmotionalReport,
                        testTag = "explore_grid_emotion"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExploreGridCard(
                        title = "足迹漫游",
                        subtitle = "生活与旅途地图",
                        emoji = "🗺️",
                        badge = "地理印章",
                        iconBg = IosColors.SystemTeal,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenFootprint,
                        testTag = "explore_grid_footprint"
                    )

                    ExploreGridCard(
                        title = "电子书册",
                        subtitle = "Markdown/TXT汇编",
                        emoji = "📖",
                        badge = "永久归档",
                        iconBg = IosColors.SystemBlue,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenBookExport,
                        testTag = "explore_grid_book"
                    )
                }
            }
        }

        // 3. 核心模块分栏二：写作与灵感
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IosSectionHeader(title = "写作与灵感")
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExploreGridCard(
                        title = "思考模板库",
                        subtitle = "晨思/晚间/九宫格",
                        emoji = "📝",
                        badge = "结构化",
                        iconBg = IosColors.SystemIndigo,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenTemplates,
                        testTag = "explore_grid_templates"
                    )

                    ExploreGridCard(
                        title = "灵感驿站",
                        subtitle = "哲思启迪与问卷",
                        emoji = "✨",
                        badge = "AI伴写",
                        iconBg = IosColors.SystemPurple,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenInspiration,
                        testTag = "explore_grid_inspiration"
                    )
                }
            }
        }

        // 4. 成就与里程碑殿堂 (Apple Awards Style)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                IosSectionHeader(title = "成就殿堂")
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            HapticFeedbackUtil.lightImpact(context)
                            onOpenBadges()
                        }
                        .testTag("explore_card_achievements")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IosColors.SystemYellow.copy(alpha = 0.18f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏆", fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "写作里程碑与勋章殿堂",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "已持续打卡 $streakDays 天 · 累计记录 ${entries.size} 篇",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = IosColors.SystemGray
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val target = when {
                                entries.size < 10 -> 10
                                entries.size < 30 -> 30
                                entries.size < 100 -> 100
                                else -> 365
                            }
                            val progress = (entries.size.toFloat() / target.toFloat()).coerceIn(0f, 1f)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(2.5.dp)),
                                    color = IosColors.SystemOrange,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${entries.size}/$target",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = IosColors.SystemGray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = IosColors.SystemGray3,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple 风格 2x2 探索宫格卡片
 */
@Composable
private fun ExploreGridCard(
    title: String,
    subtitle: String,
    emoji: String,
    badge: String,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable {
                HapticFeedbackUtil.lightImpact(context)
                onClick()
            }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBg.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = iconBg.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badge,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = iconBg
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = IosColors.SystemGray,
                    fontWeight = FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
