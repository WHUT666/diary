package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.AppThemeManager
import com.example.util.HapticFeedbackUtil

/**
 * 自定义主题生成器 (User Custom Theme Creator)
 * 允许用户自由定制专属调色板，选取主色、背景并生成专属主题
 */
@Composable
fun CustomThemeCreatorDialog(
    themeManager: AppThemeManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var themeName by remember { mutableStateOf("") }
    var isDark by remember { mutableStateOf(false) }

    // 经典莫兰迪 & 马卡龙 & 宝石强调主色调池
    val accentPalettes = remember {
        listOf(
            "薄荷碧玉" to Color(0xFF10B981),
            "天水湛蓝" to Color(0xFF007AFF),
            "克莱因蓝" to Color(0xFF2563EB),
            "落日余晖" to Color(0xFFF97316),
            "初樱暖杏" to Color(0xFFF43F5E),
            "梦幻鸢尾" to Color(0xFF8B5CF6),
            "焦糖琥珀" to Color(0xFFD97706),
            "松柏墨绿" to Color(0xFF059669),
            "深空青蓝" to Color(0xFF06B6D4),
            "优雅玫红" to Color(0xFFE11D48),
            "极客亮青" to Color(0xFF14B8A6),
            "星夜洋紫" to Color(0xFFD946EF)
        )
    }

    // 精选浅色与深色背景
    val lightBackgrounds = remember {
        listOf(
            "苹果白" to Color(0xFFF2F2F7),
            "牛皮纸白" to Color(0xFFFAF6F0),
            "晨露粉白" to Color(0xFFFDF2F4),
            "雪峰冷白" to Color(0xFFF8FAFC),
            "薰衣草雾" to Color(0xFFF6F4FC),
            "鼠尾草米" to Color(0xFFEFF5F1)
        )
    }

    val darkBackgrounds = remember {
        listOf(
            "OLED纯黑" to Color(0xFF000000),
            "深空太空灰" to Color(0xFF0B0F19),
            "深海碳墨" to Color(0xFF111827),
            "黑曜石岩" to Color(0xFF18181B),
            "暗夜深紫" to Color(0xFF170F2C)
        )
    }

    var selectedAccent by remember { mutableStateOf(accentPalettes[0].second) }
    var selectedBackground by remember { mutableStateOf(lightBackgrounds[0].second) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = selectedAccent.copy(alpha = 0.18f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = selectedAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "自创专属主题",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "莫兰迪 & 马卡龙美学定制生成",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 主题名称
                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text("主题名称") },
                    placeholder = { Text("如：初夏青桔、午夜私语...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_theme_name_input")
                )

                // 实时预览微缩卡片
                Text(
                    text = "实时微缩效果预览",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = selectedBackground
                    ),
                    border = BorderStroke(1.dp, Color(0x22000000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (themeName.isNotBlank()) themeName else "我的专属日记本",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = selectedAccent
                            ) {
                                Text(
                                    text = "标签预览",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "温润的书写底色搭配灵动点缀，记录生活的每一抹心动意趣。",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            lineHeight = 15.sp
                        )
                    }
                }

                // 深浅色切换 Switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = selectedAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isDark) "深色沉浸模式" else "明朗浅色模式",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = isDark,
                            onCheckedChange = {
                                HapticFeedbackUtil.lightImpact(context)
                                isDark = it
                                selectedBackground = if (it) darkBackgrounds[0].second else lightBackgrounds[0].second
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = selectedAccent
                            ),
                            modifier = Modifier.testTag("custom_theme_dark_switch")
                        )
                    }
                }

                // 核心强调色选择
                Text(
                    text = "选取核心强调色 (Accent Color)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accentPalettes.forEach { (name, color) ->
                        val isSelected = selectedAccent == color
                        Surface(
                            shape = CircleShape,
                            color = color,
                            border = if (isSelected) BorderStroke(2.5.dp, MaterialTheme.colorScheme.onSurface) else null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable {
                                    HapticFeedbackUtil.lightImpact(context)
                                    selectedAccent = color
                                }
                                .testTag("accent_color_$name")
                        ) {
                            if (isSelected) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 背景色调选择
                Text(
                    text = "选取底色氛围 (Background Canvas)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val currentBgList = if (isDark) darkBackgrounds else lightBackgrounds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentBgList.forEach { (name, color) ->
                        val isSelected = selectedBackground == color
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = color,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) selectedAccent else Color(0x33888888)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    HapticFeedbackUtil.lightImpact(context)
                                    selectedBackground = color
                                }
                                .testTag("bg_color_$name")
                        ) {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDark) Color.White else Color(0xFF1E293B),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    HapticFeedbackUtil.successFeedback(context)
                    val finalName = if (themeName.isNotBlank()) themeName.trim() else "我的自定色调"
                    themeManager.createAndApplyCustomTheme(
                        name = finalName,
                        accentColor = selectedAccent,
                        backgroundColor = selectedBackground,
                        isDark = isDark
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("custom_theme_create_confirm_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("保存并立即应用", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
