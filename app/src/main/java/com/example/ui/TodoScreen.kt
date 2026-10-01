package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiService
import com.example.data.TodoItem
import com.example.data.TodoPriority
import com.example.ui.components.IosColors
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
                    color = IosColors.SystemGray,
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
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
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
                                color = IosColors.SystemGray
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("todo_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosColors.SystemBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        )
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (newTodoText.isNotBlank()) IosColors.SystemBlue else IosColors.SystemGray.copy(alpha = 0.3f),
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
                                tint = Color.White,
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
                                color = IosColors.SystemGray,
                                fontWeight = FontWeight.Normal
                            )
                        )
                        TodoPriority.entries.forEach { prio ->
                            val isSelected = selectedPriority == prio
                            val prioColor = when (prio) {
                                TodoPriority.LOW -> IosColors.SystemGreen
                                TodoPriority.NORMAL -> IosColors.SystemBlue
                                TodoPriority.HIGH -> IosColors.SystemOrange
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
                                        color = if (isSelected) prioColor else IosColors.SystemGray,
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
                            color = IosColors.SystemIndigo.copy(alpha = 0.12f),
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
                                    tint = IosColors.SystemIndigo,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI 拆解",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = IosColors.SystemIndigo
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
            border = BorderStroke(0.5.dp, IosColors.SystemIndigo.copy(alpha = 0.2f)),
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
                            backgroundColor = IosColors.SystemIndigo,
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
                                color = IosColors.SystemIndigo
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = aiStatusMessage,
                                style = TextStyle(fontSize = 11.sp, color = IosColors.SystemIndigo)
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
                        color = IosColors.SystemBlue.copy(alpha = 0.1f),
                        border = BorderStroke(0.5.dp, IosColors.SystemBlue.copy(alpha = 0.25f)),
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
                                    color = IosColors.SystemBlue
                                )
                            )
                        }
                    }

                    // Action 2: 智能规划今日节奏
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IosColors.SystemOrange.copy(alpha = 0.1f),
                        border = BorderStroke(0.5.dp, IosColors.SystemOrange.copy(alpha = 0.25f)),
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
                                    color = IosColors.SystemOrange
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
                color = IosColors.SystemYellow.copy(alpha = 0.15f),
                border = BorderStroke(0.5.dp, IosColors.SystemYellow.copy(alpha = 0.3f)),
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
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = IosColors.SystemGray, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Error message banner
        aiErrorMessage?.let { err ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = IosColors.SystemPink.copy(alpha = 0.12f),
                border = BorderStroke(0.5.dp, IosColors.SystemPink.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = err,
                        style = TextStyle(fontSize = 12.sp, color = IosColors.SystemPink),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { aiErrorMessage = null }, modifier = Modifier.size(18.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = IosColors.SystemPink, modifier = Modifier.size(14.dp))
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
                    "ALL" -> "全部"
                    "PENDING" -> "待完成 ($pendingCount)"
                    "COMPLETED" -> "已完成"
                    else -> filterKey
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("todo_filter_segmented")
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Todo List or Empty State
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = IosColors.SystemBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (activeFilter == "COMPLETED") "暂无已完成的待办" else "待办清单空空如也",
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "写下计划，有条不紊开启充实的一天",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = IosColors.SystemGray
                        )
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
                        pinLabel = if (item.isCompleted) "标为未完" else "快速打勾",
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
                                color = IosColors.SystemIndigo
                            )
                        )
                        IconButton(onClick = { breakdownResults = null }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = IosColors.SystemGray)
                        }
                    }

                    breakdownTargetTask?.let {
                        Text(
                            text = "原目标：$it",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = IosColors.SystemGray
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
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("•", color = IosColors.SystemIndigo, fontWeight = FontWeight.Bold)
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
                            Text("关闭", style = TextStyle(fontSize = 14.sp, color = IosColors.SystemGray))
                        }

                        Button(
                            onClick = {
                                viewModel.addMultipleTodos(steps, priority = TodoPriority.NORMAL.name)
                                breakdownResults = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IosColors.SystemBlue),
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
 */
@Composable
fun TodoItemCard(
    item: TodoItem,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onBreakdownWithAi: () -> Unit
) {
    val prioColor = when (item.priorityEnum) {
        TodoPriority.LOW -> IosColors.SystemGreen
        TodoPriority.NORMAL -> IosColors.SystemBlue
        TodoPriority.HIGH -> IosColors.SystemOrange
    }

    val checkScale by animateFloatAsState(
        targetValue = if (item.isCompleted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "todo_check_scale"
    )

    val boxScale by animateFloatAsState(
        targetValue = if (item.isCompleted) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "todo_box_scale"
    )

    val textColor by animateColorAsState(
        targetValue = if (item.isCompleted) IosColors.SystemGray else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "todo_text_color"
    )

    val cardBgColor by animateColorAsState(
        targetValue = if (item.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "todo_card_bg"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isCompleted) 0.dp else 0.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggleCompleted() }
            .testTag("todo_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Apple Reminders Circular Checkbox with comfortable 44dp hit area
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 22.dp),
                        onClick = onToggleCompleted
                    )
                    .testTag("todo_toggle_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .scale(boxScale)
                        .clip(CircleShape)
                        .background(
                            if (item.isCompleted) IosColors.SystemBlue else Color.Transparent,
                            CircleShape
                        )
                        .border(
                            width = if (item.isCompleted) 0.dp else 1.8.dp,
                            color = if (item.isCompleted) Color.Transparent else prioColor.copy(alpha = 0.75f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (checkScale > 0.05f) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已完成",
                            tint = Color.White,
                            modifier = Modifier
                                .size(15.dp)
                                .scale(checkScale)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Text & Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium,
                        color = textColor,
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                            color = IosColors.SystemGray.copy(alpha = 0.8f)
                        )
                    )

                    // Linked to diary indicator
                    if (item.relatedDiaryId != null) {
                        Text(
                            text = "来自日记",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = IosColors.SystemBlue
                            )
                        )
                    }
                }
            }

            // AI Breakdown button (for uncompleted tasks)
            if (!item.isCompleted) {
                IconButton(
                    onClick = onBreakdownWithAi,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI 拆解任务",
                        tint = IosColors.SystemIndigo,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "删除待办",
                    tint = IosColors.SystemGray.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
