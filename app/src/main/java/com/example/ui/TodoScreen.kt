package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiService
import com.example.data.TodoItem
import com.example.data.TodoPriority
import com.example.ui.components.IosIconTile
import com.example.ui.components.IosSegmentedControl
import com.example.ui.components.SwipeableActionItem
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: DiaryViewModel,
    aiService: AiService,
    onOpenDiaryEditorWithPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todos by viewModel.filteredTodos.collectAsStateWithLifecycle()
    val activeFilter by viewModel.todoFilter.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingTodoCount.collectAsStateWithLifecycle()
    val completedCount by viewModel.completedTodoCount.collectAsStateWithLifecycle()
    val allCount by viewModel.allTodoCount.collectAsStateWithLifecycle()

    var newTodoText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TodoPriority.NORMAL) }

    // AI states
    val coroutineScope = rememberCoroutineScope()
    var isAiBusy by remember { mutableStateOf(false) }
    var aiStatusMessage by remember { mutableStateOf("") }
    var aiErrorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog for breakdown results
    var breakdownTargetTask by remember { mutableStateOf<String?>(null) }
    var breakdownResults by remember { mutableStateOf<List<String>?>(null) }

    // Dialog for daily planning guidance
    var planningAdvice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Apple Large Title Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Text(
                text = "待办备忘",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "规划日程与行动，有条不紊开启充实的一天",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            )
        }

        // Quick Input Card (Apple Inset Style)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTodoText,
                        onValueChange = { newTodoText = it },
                        placeholder = {
                            Text(
                                text = "记录待办、计划或重要事项…",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("todo_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (newTodoText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = newTodoText.isNotBlank()) {
                                HapticFeedbackUtil.lightImpact(context)
                                viewModel.addTodo(
                                    title = newTodoText,
                                    priority = selectedPriority.name
                                )
                                newTodoText = ""
                            }
                            .testTag("todo_add_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "添加",
                                tint = if (newTodoText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Priority Selector Chips (iOS Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "优先级：",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        val themePrimary = MaterialTheme.colorScheme.primary
                        val themeTertiary = MaterialTheme.colorScheme.tertiary
                        val themeSecondary = MaterialTheme.colorScheme.secondary
                        TodoPriority.entries.forEach { prio ->
                            val isSelected = selectedPriority == prio
                            val prioColor = when (prio) {
                                TodoPriority.LOW -> themeSecondary
                                TodoPriority.NORMAL -> themePrimary
                                TodoPriority.HIGH -> themeTertiary
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) prioColor.copy(alpha = 0.15f) else Color.Transparent,
                                border = BorderStroke(
                                    0.5.dp,
                                    if (isSelected) prioColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticFeedbackUtil.lightImpact(context)
                                        selectedPriority = prio
                                    }
                            ) {
                                Text(
                                    text = prio.label,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = if (isSelected) prioColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Breakdown current input if user typed a goal
                    if (newTodoText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isAiBusy) {
                                    val target = newTodoText
                                    isAiBusy = true
                                    aiStatusMessage = "AI 正在细化拆解目标…"
                                    aiErrorMessage = null
                                    coroutineScope.launch {
                                        val res = aiService.breakdownTodoTask(target)
                                        isAiBusy = false
                                        if (res.isSuccess) {
                                            breakdownTargetTask = target
                                            breakdownResults = res.getOrNull()
                                        } else {
                                            aiErrorMessage = res.exceptionOrNull()?.message
                                        }
                                    }
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI 拆解",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Smart Assistant Bar (Apple Intelligence Style)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IosIconTile(
                            icon = Icons.Default.AutoAwesome,
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            size = 24.dp,
                            iconSize = 13.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI 待办融合助理",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    if (isAiBusy) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = aiStatusMessage,
                                style = TextStyle(fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: 生成今日复盘日记
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isAiBusy) {
                                if (todos.isEmpty()) {
                                    aiErrorMessage = "当前待办列表为空，先记录几项待办吧"
                                    return@clickable
                                }
                                isAiBusy = true
                                aiStatusMessage = "AI 正在撰写复盘日记…"
                                aiErrorMessage = null
                                coroutineScope.launch {
                                    val summary = todos.joinToString("\n") {
                                        val status = if (it.isCompleted) "【已完成】" else "【进行中】"
                                        "$status ${it.title} (优先级:${it.priorityEnum.label})"
                                    }
                                    val res = aiService.generateDiaryFromTodos(summary)
                                    isAiBusy = false
                                    if (res.isSuccess) {
                                        val generatedContent = res.getOrNull() ?: ""
                                        onOpenDiaryEditorWithPrompt(generatedContent)
                                    } else {
                                        aiErrorMessage = res.exceptionOrNull()?.message
                                    }
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "📝 一键生成今日复盘日记",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Action 2: 智能规划今日节奏
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isAiBusy) {
                                val pending = todos.filter { !it.isCompleted }.map { it.title }
                                if (pending.isEmpty()) {
                                    aiErrorMessage = "暂无进行中的待办任务"
                                    return@clickable
                                }
                                isAiBusy = true
                                aiStatusMessage = "AI 正在分析任务优先级…"
                                aiErrorMessage = null
                                coroutineScope.launch {
                                    val res = aiService.suggestTodoPriorities(pending)
                                    isAiBusy = false
                                    if (res.isSuccess) {
                                        planningAdvice = res.getOrNull()
                                    } else {
                                        aiErrorMessage = res.exceptionOrNull()?.message
                                    }
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💡 专注与优先级规划",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Planning Advice Card (iOS Note Style)
        planningAdvice?.let { advice ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "💡 AI 建议规划：",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = advice,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        )
                    }
                    IconButton(onClick = { planningAdvice = null }, modifier = Modifier.size(20.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Error message banner
        aiErrorMessage?.let { err ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = err,
                        style = TextStyle(fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { aiErrorMessage = null }, modifier = Modifier.size(18.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // iOS Segmented Control for Todo Filter
        Spacer(modifier = Modifier.height(8.dp))
        IosSegmentedControl(
            items = listOf("ALL", "PENDING", "COMPLETED"),
            selectedItem = activeFilter,
            onItemSelected = { viewModel.setTodoFilter(it) },
            itemLabel = { filterKey ->
                when (filterKey) {
                    "ALL" -> if (allCount > 0) "全部 ($allCount)" else "全部"
                    "PENDING" -> "待完成 ($pendingCount)"
                    "COMPLETED" -> if (completedCount > 0) "已完成 ($completedCount)" else "已完成"
                    else -> filterKey
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("todo_filter_segmented")
        )

        // Apple Reminders Style Completion Progress Summary
        if (allCount > 0) {
            val progressFraction = (completedCount.toFloat() / allCount.toFloat()).coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(
                targetValue = progressFraction,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "todo_progress_anim"
            )

            val themePrimary = MaterialTheme.colorScheme.primary

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (pendingCount == 0 && allCount > 0) Icons.Default.TaskAlt else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = themePrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (pendingCount == 0 && allCount > 0) "🎉 今日待办全部达成！" else "完成进度：$completedCount / $allCount 项",
                                style = TextStyle(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = themePrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Apple Reminders Capsule Progress Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(themePrimary)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))

        // Todo List or Empty State
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    val emptyIcon = when (activeFilter) {
                        "COMPLETED" -> Icons.Default.TaskAlt
                        "PENDING" -> Icons.Default.CheckCircle
                        else -> Icons.Default.Checklist
                    }
                    val emptyTint = when (activeFilter) {
                        "COMPLETED" -> MaterialTheme.colorScheme.primary
                        "PENDING" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                    val emptyTitle = when (activeFilter) {
                        "COMPLETED" -> "暂无已完成的待办"
                        "PENDING" -> if (allCount > 0) "太棒了，进行中事项全部搞定！" else "暂无待完成事项"
                        else -> "待办清单空空如也"
                    }
                    val emptySubtitle = when (activeFilter) {
                        "COMPLETED" -> "点击待办圆圈标记完成，在此回顾每一份小成就"
                        "PENDING" -> if (allCount > 0) "没有未完任务，享受悠闲时光或规划新日程" else "写下计划，有条不紊开启充实的一天"
                        else -> "写下计划，有条不紊开启充实的一天"
                    }

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = emptyTint.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = emptyIcon,
                                contentDescription = null,
                                tint = emptyTint,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = emptyTitle,
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = emptySubtitle,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("todo_items_list"),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todos, key = { it.id }) { item ->
                    SwipeableActionItem(
                        modifier = Modifier.animateItem(),
                        onDelete = {
                            viewModel.deleteTodo(item)
                        },
                        onPin = {
                            if (!item.isCompleted) {
                                HapticFeedbackUtil.successFeedback(context)
                            } else {
                                HapticFeedbackUtil.lightImpact(context)
                            }
                            viewModel.toggleTodo(item)
                        },
                        isPinned = item.isCompleted,
                        pinLabel = if (item.isCompleted) "标为未完" else "标记完成",
                        pinIcon = if (item.isCompleted) Icons.Default.Undo else Icons.Default.Check,
                        deleteLabel = "删除待办"
                    ) {
                        TodoItemCard(
                            item = item,
                            onToggleCompleted = {
                                if (!item.isCompleted) {
                                    HapticFeedbackUtil.successFeedback(context)
                                } else {
                                    HapticFeedbackUtil.lightImpact(context)
                                }
                                viewModel.toggleTodo(item)
                            },
                            onDelete = { viewModel.deleteTodo(item) },
                            onBreakdownWithAi = {
                                isAiBusy = true
                                aiStatusMessage = "AI 正在拆解子任务…"
                                aiErrorMessage = null
                                coroutineScope.launch {
                                    val res = aiService.breakdownTodoTask(item.title)
                                    isAiBusy = false
                                    if (res.isSuccess) {
                                        breakdownTargetTask = item.title
                                        breakdownResults = res.getOrNull()
                                    } else {
                                        aiErrorMessage = res.exceptionOrNull()?.message
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // AI Breakdown Steps Dialog (Apple Modal Style)
    breakdownResults?.let { steps ->
        Dialog(onDismissRequest = { breakdownResults = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ AI 任务拆解",
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        IconButton(onClick = { breakdownResults = null }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    breakdownTargetTask?.let {
                        Text(
                            text = "原目标：$it",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "建议的具体行动步骤：",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    steps.forEach { step ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { breakdownResults = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("关闭", style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                        }

                        Button(
                            onClick = {
                                viewModel.addMultipleTodos(steps, priority = TodoPriority.NORMAL.name)
                                breakdownResults = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("一键全加入待办", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Apple Reminders 风格待办卡片组件
 * 严格遵循 iOS 17/18 经典提醒事项设计范式与全局主题色彩规范：
 * 1. 选中微动效：
 *    - 触感微弹跳 (0.82f 瞬态挤压 -> 1.15f 弹性弹射 -> 1.0f 弹簧定型)，且仅在用户手动点击交互时触发，滑动与初始化绝不抖动或频闪。
 *    - 勾选涌现：Checkmark 伴随 spring 缩放与 alpha 平滑显现。
 * 2. 主题色彩与质感一体化：
 *    - Checkbox 与完成态徽章统一采用当前主题核心强调色 (themePrimary)，无论切换至暖阳、鼠尾草绿还是初樱主题，配色绝对纯正统一。
 *    - 优先级色彩映射主题系统色调（高优先级使用 tertiary 暖橙/强调色，低优先级使用 secondary 柔色）。
 * 3. 卡片外观消除发脏感：
 *    - 保持纯净实色卡片底色 (surface)，摒弃原先 0.45f 的半透明发灰发脏色块；
 *    - 边框与阴影平滑过渡，字体颜色采用 iOS 标准二级文本色 (onSurfaceVariant)，配合优雅精细删除线。
 * 4. 丰富完备的操作与信息反馈：
 *    - 已完成状态附带清晰实体【✓ 已完成】徽章、精确完成时间与原任务级别标签；
 *    - 已完成卡片右侧提供显式「撤销完成」按钮，让误触后一键复原丝滑直观；
 *    - 待完成状态保留「AI 拆解」与「删除待办」快捷操作。
 */
@Composable
fun TodoItemCard(
    item: TodoItem,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onBreakdownWithAi: () -> Unit
) {
    val themePrimary = MaterialTheme.colorScheme.primary
    val themeTertiary = MaterialTheme.colorScheme.tertiary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val prioColor = when (item.priorityEnum) {
        TodoPriority.LOW -> themeSecondary
        TodoPriority.NORMAL -> themePrimary
        TodoPriority.HIGH -> themeTertiary
    }

    val coroutineScope = rememberCoroutineScope()
    val checkboxScale = remember { Animatable(1f) }

    val handleToggle: () -> Unit = {
        coroutineScope.launch {
            checkboxScale.animateTo(0.82f, tween(60))
            checkboxScale.animateTo(1.15f, tween(110))
            checkboxScale.animateTo(
                1.0f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        onToggleCompleted()
    }

    // Checkmark scale spring animation
    val checkScale by animateFloatAsState(
        targetValue = if (item.isCompleted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "todo_check_scale"
    )

    // Checkbox fill color: smooth fill with theme primary color
    val checkboxFillColor by animateColorAsState(
        targetValue = if (item.isCompleted) themePrimary else Color.Transparent,
        animationSpec = tween(180),
        label = "todo_checkbox_fill"
    )

    // Checkbox border: crisp ring when unchecked, matching fill when completed
    val checkboxBorderColor by animateColorAsState(
        targetValue = if (item.isCompleted) themePrimary else prioColor.copy(alpha = 0.85f),
        animationSpec = tween(180),
        label = "todo_checkbox_border"
    )

    // Card background: Solid theme surface (no murky semi-transparency!)
    val cardBgColor = MaterialTheme.colorScheme.surface

    // Card border: gentle hairline stroke
    val cardBorderColor by animateColorAsState(
        targetValue = if (item.isCompleted) {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
        },
        animationSpec = tween(220),
        label = "todo_card_border"
    )

    // Title text color: iOS secondary text color for completed
    val titleTextColor by animateColorAsState(
        targetValue = if (item.isCompleted) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "todo_title_color"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(0.5.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isCompleted) 0.dp else 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = themePrimary.copy(alpha = 0.12f)),
                onClick = handleToggle
            )
            .testTag("todo_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Apple Reminders Circular Checkbox with comfortable touch area
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 20.dp, color = themePrimary.copy(alpha = 0.25f)),
                        onClick = handleToggle
                    )
                    .testTag("todo_toggle_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .scale(checkboxScale.value)
                        .clip(CircleShape)
                        .background(checkboxFillColor, CircleShape)
                        .border(
                            width = 1.8.dp,
                            color = checkboxBorderColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checkScale > 0.05f) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已完成",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(13.5.dp)
                                .scale(checkScale)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text & Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium,
                        color = titleTextColor,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                if (item.isCompleted) {
                    // Completed State: Clean, polished iOS Reminders badges & timestamp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Completed badge matching theme
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = themePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "✓ 已完成",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themePrimary
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }

                        val completedTime = item.formattedCompletedDate ?: item.formattedCreatedDate
                        Text(
                            text = "完成于 $completedTime",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Normal
                            )
                        )

                        if (item.priorityEnum != TodoPriority.NORMAL) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = prioColor.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = item.priorityEnum.label,
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = prioColor.copy(alpha = 0.85f)
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                )
                            }
                        }

                        if (item.relatedDiaryId != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = themeSecondary.copy(alpha = 0.10f)
                            ) {
                                Text(
                                    text = "来自日记",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Pending State: Clear informative badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Priority Badge (iOS pill)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = prioColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = item.priorityEnum.label,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = prioColor
                                ),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }

                        // Created time
                        Text(
                            text = item.formattedCreatedDate,
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        )

                        // Linked to diary indicator
                        if (item.relatedDiaryId != null) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = themeSecondary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "日记关联",
                                    style = TextStyle(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = themeSecondary
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.5.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (item.isCompleted) {
                // Quick Undo Button for Completed Tasks
                Surface(
                    shape = CircleShape,
                    color = themePrimary.copy(alpha = 0.10f),
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable(
                            onClick = handleToggle,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true, color = themePrimary.copy(alpha = 0.25f))
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "撤销完成，重回待办",
                            tint = themePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            } else {
                // AI Breakdown button (only for uncompleted tasks)
                IconButton(
                    onClick = onBreakdownWithAi,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI 拆解任务",
                        tint = themePrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "删除待办",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (item.isCompleted) 0.5f else 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
