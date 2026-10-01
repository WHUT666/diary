package com.example.ui.export

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DiaryEntry
import com.example.data.DiaryExportManager
import com.example.util.HapticFeedbackUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryBookExportDialog(
    allEntries: List<DiaryEntry>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var bookTitle by remember { mutableStateOf("我的生活日记全集") }
    var selectedScopeIndex by remember { mutableStateOf(0) } // 0: 全部, 1: 近30天, 2: 近半年, 3: 本年度
    var selectedFormatIndex by remember { mutableStateOf(0) } // 0: Markdown (.md), 1: 纯文本 (.txt)

    val currentMillis = System.currentTimeMillis()
    val filteredEntries = remember(allEntries, selectedScopeIndex) {
        when (selectedScopeIndex) {
            1 -> allEntries.filter { it.createdAt >= currentMillis - 30L * 86400000L }
            2 -> allEntries.filter { it.createdAt >= currentMillis - 180L * 86400000L }
            3 -> allEntries.filter { it.createdAt >= currentMillis - 365L * 86400000L }
            else -> allEntries
        }
    }

    val totalWords = remember(filteredEntries) { filteredEntries.sumOf { it.wordCount } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("diary_book_export_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar with deep sapphire gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF2563EB),
                                    Color(0xFF1D4ED8)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📖", fontSize = 20.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "导出日记电子书",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "支持 Markdown / TXT 排版归档",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                HapticFeedbackUtil.lightImpact(context)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Field
                    Column {
                        Text(
                            text = "电子书册标题",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = bookTitle,
                            onValueChange = { bookTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Scope Selector
                    Column {
                        Text(
                            text = "收录时间范围",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("全部日记", "近 30 天", "近半年", "本年度").forEachIndexed { index, title ->
                                FilterChip(
                                    selected = selectedScopeIndex == index,
                                    onClick = {
                                        HapticFeedbackUtil.lightImpact(context)
                                        selectedScopeIndex = index
                                    },
                                    label = { Text(title) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2563EB),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Format Selector
                    Column {
                        Text(
                            text = "导出文件格式",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Markdown option
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selectedFormatIndex == 0) Color(0xFF2563EB).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (selectedFormatIndex == 0) 1.5.dp else 1.dp,
                                    color = if (selectedFormatIndex == 0) Color(0xFF2563EB) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        HapticFeedbackUtil.lightImpact(context)
                                        selectedFormatIndex = 0
                                    }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("📝 Markdown (.md)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("包含优雅排版、粗体元数据与分割线，适合 Notion/Obsidian 等笔记软件导入", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // TXT option
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (selectedFormatIndex == 1) Color(0xFF2563EB).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (selectedFormatIndex == 1) 1.5.dp else 1.dp,
                                    color = if (selectedFormatIndex == 1) Color(0xFF2563EB) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        HapticFeedbackUtil.lightImpact(context)
                                        selectedFormatIndex = 1
                                    }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("📄 纯文本 (.txt)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("通用标准纯文本格式，可在任何设备、记事本或打印机直接打开阅读", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Summary Stats Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("收录篇数", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${filteredEntries.size} 篇", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("总计字数", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$totalWords 字", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                            }
                        }
                    }
                }

                // Bottom Export Button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                        Button(
                            onClick = {
                                HapticFeedbackUtil.mediumImpact(context)
                                if (filteredEntries.isEmpty()) {
                                    Toast.makeText(context, "当前范围内没有可导出的日记", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                val fileName = if (selectedFormatIndex == 0) {
                                    "${bookTitle}_$timeStamp.md"
                                } else {
                                    "${bookTitle}_$timeStamp.txt"
                                }

                                val content = if (selectedFormatIndex == 0) {
                                    DiaryExportManager.generateMarkdownBook(filteredEntries, bookTitle)
                                } else {
                                    DiaryExportManager.generatePlainTextBook(filteredEntries, bookTitle)
                                }

                                val mimeType = if (selectedFormatIndex == 0) "text/markdown" else "text/plain"
                                val success = DiaryExportManager.shareExportedFile(context, content, fileName, mimeType)
                                if (success) {
                                    Toast.makeText(context, "电子书已生成并调起分享！", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "导出失败，请重试", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("export_book_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("一键导出与分享电子书", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
