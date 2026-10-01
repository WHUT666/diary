package com.example.ui.poster

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.DiaryEntry
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.launch

@Composable
fun DiaryPosterDialog(
    entry: DiaryEntry,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedThemeType by remember { mutableStateOf(PosterThemeType.PARCHMENT) }
    val currentTheme = remember(selectedThemeType) { DiaryPosterTheme.fromType(selectedThemeType) }

    var isExporting by remember { mutableStateOf(false) }
    var exportActionText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "精美日记海报卡片",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "选择风格排版，一键生成高清长图",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isExporting,
                        modifier = Modifier
                            .testTag("poster_dialog_close")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Theme Selector Horizontal Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiaryPosterTheme.AllThemes.forEach { theme ->
                        val isSelected = theme.type == selectedThemeType
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                HapticFeedbackUtil.lightImpact(context)
                                selectedThemeType = theme.type
                            },
                            label = {
                                Text(
                                    text = "${theme.type.iconEmoji} ${theme.type.title}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("poster_theme_${theme.type.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable Live Poster Preview Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(currentTheme.backgroundBrush)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Visual Poster Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = currentTheme.cardBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.cardBorderColor),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Header Date Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = entry.dayOfMonth,
                                    fontSize = 46.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = currentTheme.accentColor,
                                    letterSpacing = (-1).sp
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = entry.monthYear,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = currentTheme.textSecondary
                                    )
                                    Text(
                                        text = "${entry.dayOfWeek} · ${entry.formattedTime}",
                                        fontSize = 13.sp,
                                        color = currentTheme.textSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(
                                color = currentTheme.cardBorderColor,
                                thickness = 0.8.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Mood, Weather & Location Badges
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Mood Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = currentTheme.pillBackground
                                ) {
                                    Text(
                                        text = "${entry.mood} ${entry.moodLabel}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = currentTheme.pillTextColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                // Weather Badge
                                if (entry.weather.isNotBlank()) {
                                    val tempStr = if (entry.temperature.isNotBlank()) " ${entry.temperature}" else ""
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = currentTheme.pillBackground
                                    ) {
                                        Text(
                                            text = "${entry.weather} ${entry.weatherLabel}$tempStr",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = currentTheme.pillTextColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                // Category Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = currentTheme.pillBackground
                                ) {
                                    Text(
                                        text = "🔖 ${entry.category}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = currentTheme.pillTextColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                // Location Badge
                                if (entry.location.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = currentTheme.pillBackground
                                    ) {
                                        Text(
                                            text = "📍 ${entry.location}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = currentTheme.pillTextColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Title (if present)
                            if (entry.title.isNotBlank()) {
                                Text(
                                    text = entry.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = currentTheme.textPrimary,
                                    lineHeight = 26.sp,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Body Content
                            Text(
                                text = entry.cleanPosterContent.ifBlank { "（无文字记录）" },
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = currentTheme.textPrimary,
                                letterSpacing = 0.2.sp
                            )

                            // Photos Showcase (if present)
                            val photosToShow = entry.allImages.take(2)
                            if (photosToShow.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    photosToShow.forEach { uriStr ->
                                        AsyncImage(
                                            model = uriStr,
                                            contentDescription = "日记附图",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, currentTheme.cardBorderColor, RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(
                                color = currentTheme.cardBorderColor,
                                thickness = 0.8.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Quote & Seal Stamp Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Quote & App Watermark
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FormatQuote,
                                            contentDescription = null,
                                            tint = currentTheme.accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentTheme.defaultQuote,
                                            fontSize = 12.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = currentTheme.textSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "心语日记 · 本地私密生活志",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = currentTheme.textPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Traditional Seal Stamp Motif
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(2.dp, currentTheme.sealBorderColor),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(3.dp)
                                            .border(0.5.dp, currentTheme.sealBorderColor, RoundedCornerShape(4.dp))
                                    ) {
                                        Text(
                                            text = currentTheme.sealText.take(4),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = currentTheme.sealTextColor,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 10.sp,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Export Buttons Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Progress / Status indicator
                        AnimatedVisibility(
                            visible = isExporting,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = exportActionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Save to Gallery Button
                            OutlinedButton(
                                onClick = {
                                    if (isExporting) return@OutlinedButton
                                    HapticFeedbackUtil.lightImpact(context)
                                    isExporting = true
                                    exportActionText = "正在生成高清海报并保存至相册…"
                                    coroutineScope.launch {
                                        try {
                                            val bitmap = DiaryPosterExporter.generatePosterBitmap(context, entry, currentTheme)
                                            val result = DiaryPosterExporter.saveBitmapToGallery(context, bitmap, entry.title)
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "✨ 已成功保存至系统相册！", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "保存失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "海报生成失败: ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            isExporting = false
                                        }
                                    }
                                },
                                enabled = !isExporting,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("poster_save_gallery_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("保存至相册", fontWeight = FontWeight.SemiBold)
                            }

                            // Direct Share Button
                            Button(
                                onClick = {
                                    if (isExporting) return@Button
                                    HapticFeedbackUtil.lightImpact(context)
                                    isExporting = true
                                    exportActionText = "正在渲染并调起分享…"
                                    coroutineScope.launch {
                                        try {
                                            val bitmap = DiaryPosterExporter.generatePosterBitmap(context, entry, currentTheme)
                                            val result = DiaryPosterExporter.shareBitmap(context, bitmap, entry)
                                            if (result.isFailure) {
                                                Toast.makeText(context, "分享失败: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "海报渲染失败: ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            isExporting = false
                                        }
                                    }
                                },
                                enabled = !isExporting,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("poster_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("立即分享", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
