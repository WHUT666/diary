package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.AiService
import com.example.ai.DailyInspiration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class InspirationTab(val label: String) {
    DAILY_QUOTE("今日格言"),
    ON_THIS_DAY("历史今日"),
    MINDFULNESS("正念觉察"),
    FRAGMENT_TO_DIARY("碎片整理"),
    MOOD_CARE("情绪陪伴")
}

@Composable
fun AiInspirationStationDialog(
    aiService: AiService,
    onDismiss: () -> Unit,
    onStartNewDiary: (starterContent: String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(InspirationTab.DAILY_QUOTE) }

    // Tab 1 state
    var dailyQuote by remember { mutableStateOf<DailyInspiration?>(null) }
    var isQuoteLoading by remember { mutableStateOf(false) }

    // Tab 2 state
    var onThisDayText by remember { mutableStateOf<String?>(null) }
    var isOnThisDayLoading by remember { mutableStateOf(false) }

    // Tab 3 state
    var mindfulnessText by remember { mutableStateOf<String?>(null) }
    var isMindfulnessLoading by remember { mutableStateOf(false) }

    // Tab 4 state
    var fragmentsInput by remember { mutableStateOf("") }
    var organizedDiaryResult by remember { mutableStateOf<String?>(null) }
    var isOrganizingLoading by remember { mutableStateOf(false) }

    // Tab 5 state
    var selectedMoodForCare by remember { mutableStateOf("疲惫") }
    var moodSnippetInput by remember { mutableStateOf("") }
    var moodCareAdvice by remember { mutableStateOf<String?>(null) }
    var isMoodCareLoading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val todayDateStr = remember {
        val sdf = SimpleDateFormat("MM月dd日", Locale.CHINESE)
        sdf.format(Date())
    }

    // Auto-fetch daily quote when opened
    LaunchedEffect(Unit) {
        if (dailyQuote == null) {
            isQuoteLoading = true
            val res = aiService.fetchDailyInspiration()
            isQuoteLoading = false
            if (res.isSuccess) {
                dailyQuote = res.getOrNull()
            } else {
                errorMessage = res.exceptionOrNull()?.message
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI 灵感驿站",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "多元信息获取 · 激发每日记录灵感",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("inspiration_station_close")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InspirationTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTab = tab
                                errorMessage = null
                            },
                            label = { Text(tab.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error alert if any
                errorMessage?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // TAB 1: DAILY QUOTE
                if (selectedTab == InspirationTab.DAILY_QUOTE) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "每日一语 · 生活格言",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        isQuoteLoading = true
                                        coroutineScope.launch {
                                            val res = aiService.fetchDailyInspiration()
                                            isQuoteLoading = false
                                            if (res.isSuccess) dailyQuote = res.getOrNull()
                                            else errorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "换一句",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isQuoteLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                }
                            } else {
                                dailyQuote?.let { quoteItem ->
                                    Text(
                                        text = "“${quoteItem.quote}”",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "—— ${quoteItem.author}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = quoteItem.reflection,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onStartNewDiary(
                                                "【今日格言】“${quoteItem.quote}” —— ${quoteItem.author}\n\n${quoteItem.reflection}\n\n【我的感悟】\n"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("以此格言写新日记", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: ON THIS DAY
                if (selectedTab == InspirationTab.ON_THIS_DAY) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "历史上的今天 ($todayDateStr)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                IconButton(
                                    onClick = {
                                        isOnThisDayLoading = true
                                        coroutineScope.launch {
                                            val res = aiService.fetchOnThisDayInsight(todayDateStr)
                                            isOnThisDayLoading = false
                                            if (res.isSuccess) onThisDayText = res.getOrNull()
                                            else errorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "刷新", modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isOnThisDayLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                }
                            } else if (onThisDayText != null) {
                                Text(
                                    text = onThisDayText ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onStartNewDiary(
                                            "【历史上的今天 · $todayDateStr】\n$onThisDayText\n\n【今日所思】\n"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("以历史之眼写日记", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = "点击下方按钮，AI 将为你探索历史上与今天相关的历史故事与写作视角。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        isOnThisDayLoading = true
                                        coroutineScope.launch {
                                            val res = aiService.fetchOnThisDayInsight(todayDateStr)
                                            isOnThisDayLoading = false
                                            if (res.isSuccess) onThisDayText = res.getOrNull()
                                            else errorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("探索历史上的今天", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // TAB 3: MINDFULNESS PROMPT
                if (selectedTab == InspirationTab.MINDFULNESS) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "正念觉察 · 专注当下感受",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (isMindfulnessLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                }
                            } else if (mindfulnessText != null) {
                                Text(
                                    text = mindfulnessText ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        onStartNewDiary(
                                            "【今日正念观察】\n$mindfulnessText\n\n【我的真实体验】\n"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(imageVector = Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("开始记录正念感受", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    text = "生活经常在匆忙中溜走。让 AI 为你设计一个简短的五感或心灵正念觉察练习，慢下来感受生活。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        isMindfulnessLoading = true
                                        coroutineScope.launch {
                                            val res = aiService.fetchMindfulnessPrompt()
                                            isMindfulnessLoading = false
                                            if (res.isSuccess) mindfulnessText = res.getOrNull()
                                            else errorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("获取今日正念练习", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // TAB 4: FRAGMENT TO DIARY
                if (selectedTab == InspirationTab.FRAGMENT_TO_DIARY) {
                    Column {
                        Text(
                            text = "输入散乱的备忘或想法碎片：",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = fragmentsInput,
                            onValueChange = { fragmentsInput = it },
                            placeholder = {
                                Text("例如：\n- 早上喝了燕麦拿铁\n- 遇到老同学聊了几句很亲切\n- 傍晚晚霞是粉紫色的特别美\n- 感到平淡生活也很值得珍惜")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("fragments_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (fragmentsInput.isNotBlank()) {
                                    isOrganizingLoading = true
                                    organizedDiaryResult = null
                                    coroutineScope.launch {
                                        val res = aiService.convertFragmentToDiary(fragmentsInput)
                                        isOrganizingLoading = false
                                        if (res.isSuccess) organizedDiaryResult = res.getOrNull()
                                        else errorMessage = res.exceptionOrNull()?.message
                                    }
                                }
                            },
                            enabled = fragmentsInput.isNotBlank() && !isOrganizingLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isOrganizingLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI 正在编织日记…")
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("一键串联成篇", fontWeight = FontWeight.Bold)
                            }
                        }

                        organizedDiaryResult?.let { organizedText ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "✨ AI 整理成篇结果：",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = organizedText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        lineHeight = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onStartNewDiary(organizedText)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("应用并保存到新日记", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 5: MOOD CARE
                if (selectedTab == InspirationTab.MOOD_CARE) {
                    Column {
                        Text(
                            text = "选择你此刻的心情状态：",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("疲惫", "焦虑", "低落", "迷茫", "生气", "孤独", "平静", "感恩").forEach { mood ->
                                FilterChip(
                                    selected = selectedMoodForCare == mood,
                                    onClick = { selectedMoodForCare = mood },
                                    label = { Text(mood) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = moodSnippetInput,
                            onValueChange = { moodSnippetInput = it },
                            placeholder = { Text("（选填）可以说说为什么会有这种感觉，或者今天遇到了什么…") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                isMoodCareLoading = true
                                moodCareAdvice = null
                                coroutineScope.launch {
                                    val res = aiService.generateMoodHealingAdvice(selectedMoodForCare, moodSnippetInput)
                                    isMoodCareLoading = false
                                    if (res.isSuccess) moodCareAdvice = res.getOrNull()
                                    else errorMessage = res.exceptionOrNull()?.message
                                }
                            },
                            enabled = !isMoodCareLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isMoodCareLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("正在倾听与整理…")
                            } else {
                                Icon(imageVector = Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("获取温暖陪伴与放松小建议", fontWeight = FontWeight.Bold)
                            }
                        }

                        moodCareAdvice?.let { advice ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "💌 温暖陪伴寄语：",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = advice,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        lineHeight = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            onDismiss()
                                            onStartNewDiary(
                                                "【今日心情 · $selectedMoodForCare】\n$moodSnippetInput\n\n【给自己的温柔回响】\n$advice\n"
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("将这份温暖记入日记", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
