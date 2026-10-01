package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.AiExtractedMetadata
import com.example.ai.AiService
import com.example.data.AvailableMoods
import com.example.data.AvailableWeathers
import com.example.data.DefaultCategories
import com.example.data.DiaryDraft
import com.example.data.DiaryDraftManager
import com.example.data.DiaryEntry
import com.example.data.InspirationalPrompts
import com.example.data.JournalTemplate
import com.example.ui.audio.AudioNoteEditorSection
import com.example.ui.template.JournalTemplateDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AutoSaveStatus {
    IDLE,
    SAVING,
    SAVED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditorDialog(
    existingEntry: DiaryEntry?,
    initialPrompt: String?,
    initialLocation: String? = null,
    initialWeatherEmoji: String? = null,
    initialWeatherLabel: String? = null,
    initialTemperature: String? = null,
    initialTimestamp: Long? = null,
    draftManager: DiaryDraftManager? = null,
    aiService: AiService,
    onDismiss: () -> Unit,
    onAddExtractedTodos: ((List<String>) -> Unit)? = null,
    onSave: (
        id: Long,
        title: String,
        content: String,
        mood: String,
        moodLabel: String,
        weather: String,
        weatherLabel: String,
        temperature: String,
        location: String,
        imagesJson: String,
        audioPath: String,
        audioDurationSec: Int,
        category: String,
        timestamp: Long,
        isFavorite: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(existingEntry?.title ?: "") }
    var contentValue by remember {
        val initialText = existingEntry?.content ?: (initialPrompt?.let { "$it\n\n" } ?: "")
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }
    var selectedMood by remember {
        mutableStateOf(
            AvailableMoods.find { it.emoji == existingEntry?.mood } ?: AvailableMoods.first()
        )
    }
    var selectedWeather by remember {
        mutableStateOf(
            AvailableWeathers.find {
                it.emoji == existingEntry?.weather ||
                (existingEntry == null && (it.emoji == initialWeatherEmoji || it.label == initialWeatherLabel))
            } ?: AvailableWeathers.first()
        )
    }
    var temperature by remember {
        mutableStateOf(
            existingEntry?.temperature ?: initialTemperature ?: selectedWeather.defaultTemp
        )
    }
    var location by remember {
        mutableStateOf(existingEntry?.location ?: initialLocation ?: "")
    }
    var images by remember {
        mutableStateOf(existingEntry?.imageList ?: emptyList())
    }
    var audioPath by remember { mutableStateOf(existingEntry?.audioPath ?: "") }
    var audioDurationSec by remember { mutableStateOf(existingEntry?.audioDurationSec ?: 0) }
    var selectedCategory by remember {
        mutableStateOf(existingEntry?.category ?: "日常")
    }
    var isFavorite by remember { mutableStateOf(existingEntry?.isFavorite ?: false) }
    var currentTimestamp by remember {
        mutableLongStateOf(existingEntry?.createdAt ?: initialTimestamp ?: System.currentTimeMillis())
    }
    var isPreviewMode by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }
    var showInspirationSheet by remember { mutableStateOf(false) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var isWeatherCustomExpanded by remember { mutableStateOf(false) }

    // AI Assistant State
    var isAiWorking by remember { mutableStateOf(false) }
    var aiActionTitle by remember { mutableStateOf("") }
    var aiGeneratedResult by remember { mutableStateOf<String?>(null) }
    var aiExtractedMetadata by remember { mutableStateOf<AiExtractedMetadata?>(null) }
    var extractedTodosState by remember { mutableStateOf<List<String>?>(null) }
    var extractedTodosSuccess by remember { mutableStateOf(false) }
    var aiErrorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val activeDraftManager = remember(draftManager) {
        draftManager ?: DiaryDraftManager(context)
    }
    val draftKey = remember(existingEntry) {
        DiaryDraftManager.keyForEntry(existingEntry?.id)
    }

    // 检查是否存在未保存的本地草稿
    var availableDraft by remember {
        val saved = activeDraftManager.getDraft(draftKey)
        val shouldOffer = if (existingEntry == null) {
            saved != null && saved.isNotEmpty()
        } else {
            saved != null && saved.isNotEmpty() && saved.lastSavedAt > (existingEntry.updatedAt)
        }
        mutableStateOf(if (shouldOffer) saved else null)
    }

    // 自动保存状态
    var autoSaveState by remember { mutableStateOf(AutoSaveStatus.IDLE) }
    var lastAutoSavedTime by remember { mutableStateOf<String?>(null) }
    var lastSavedTimestamp by remember { mutableLongStateOf(0L) }
    var hasLocalChanges by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    fun saveDraftSnapshot() {
        val rawText = contentValue.text
        val hasContent = rawText.isNotBlank() || title.isNotBlank() || images.isNotEmpty()
        if (!hasContent) {
            if (hasLocalChanges) {
                activeDraftManager.clearDraft(draftKey)
                hasLocalChanges = false
                autoSaveState = AutoSaveStatus.IDLE
            }
            return
        }

        val now = System.currentTimeMillis()
        val draft = DiaryDraft(
            draftKey = draftKey,
            targetEntryId = existingEntry?.id ?: 0L,
            title = title,
            content = rawText,
            mood = selectedMood.emoji,
            moodLabel = selectedMood.label,
            weather = selectedWeather.emoji,
            weatherLabel = selectedWeather.label,
            temperature = temperature,
            location = location,
            images = images,
            category = selectedCategory,
            timestamp = currentTimestamp,
            isFavorite = isFavorite,
            lastSavedAt = now
        )
        activeDraftManager.saveDraft(draft)
        lastSavedTimestamp = now
        val timeSdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        lastAutoSavedTime = timeSdf.format(Date(now))
        autoSaveState = AutoSaveStatus.SAVED
    }

    // 页面初始化延时就绪，避免刚打开时误触发空保存
    LaunchedEffect(Unit) {
        delay(400L)
        isInitialized = true
    }

    // 当用户输入或修改元数据时，每隔几秒钟自动保存草稿到本地
    LaunchedEffect(
        title,
        contentValue.text,
        selectedMood,
        selectedWeather,
        temperature,
        location,
        images,
        selectedCategory,
        isFavorite,
        isInitialized
    ) {
        if (!isInitialized) return@LaunchedEffect

        val rawText = contentValue.text
        val hasContent = rawText.isNotBlank() || title.isNotBlank() || images.isNotEmpty()

        if (!hasContent) {
            if (hasLocalChanges) {
                activeDraftManager.clearDraft(draftKey)
                hasLocalChanges = false
                autoSaveState = AutoSaveStatus.IDLE
            }
            return@LaunchedEffect
        }

        hasLocalChanges = true
        autoSaveState = AutoSaveStatus.SAVING

        val now = System.currentTimeMillis()
        // 若已连续输入超4秒，则缩短等待即时保存，否则停顿2秒自动保存
        if (now - lastSavedTimestamp >= 4000L && lastSavedTimestamp > 0L) {
            delay(1000L)
        } else {
            delay(2000L)
        }

        saveDraftSnapshot()
    }

    // 后台兜底定时器（每5秒）：确保即使用户持续快速输入不停顿，也能周期性保存至本地
    LaunchedEffect(isInitialized) {
        if (!isInitialized) return@LaunchedEffect
        while (isActive) {
            delay(5000L)
            val now = System.currentTimeMillis()
            val rawText = contentValue.text
            val hasContent = rawText.isNotBlank() || title.isNotBlank() || images.isNotEmpty()
            if (hasLocalChanges && hasContent && autoSaveState == AutoSaveStatus.SAVING && (now - lastSavedTimestamp >= 4000L)) {
                saveDraftSnapshot()
            }
        }
    }

    // 退出或组件销毁时，立即冲刷保存未同步的草稿更改
    DisposableEffect(draftKey) {
        onDispose {
            val rawText = contentValue.text
            val hasContent = rawText.isNotBlank() || title.isNotBlank() || images.isNotEmpty()
            if (hasLocalChanges && hasContent) {
                saveDraftSnapshot()
            }
        }
    }

    val handleDismiss = {
        val rawText = contentValue.text
        val hasContent = rawText.isNotBlank() || title.isNotBlank() || images.isNotEmpty()
        if (hasLocalChanges && hasContent) {
            showExitConfirmDialog = true
        } else {
            onDismiss()
        }
    }

    val rawContent = contentValue.text
    val charCount = rawContent.length
    val wordCount = DiaryEntry.calculateWordCount(rawContent)

    val dateFormatted = remember(currentTimestamp) {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 EEEE HH:mm", Locale.CHINESE)
        sdf.format(Date(currentTimestamp))
    }

    Dialog(
        onDismissRequest = handleDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = handleDismiss,
                        modifier = Modifier
                            .testTag("editor_close_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "取消编辑",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = if (existingEntry == null) "写日记" else "编辑日记",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        AnimatedVisibility(visible = autoSaveState != AutoSaveStatus.IDLE) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                when (autoSaveState) {
                                    AutoSaveStatus.SAVING -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            strokeWidth = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "草稿保存中...",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    AutoSaveStatus.SAVED -> {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (lastAutoSavedTime != null) "已自动保存 $lastAutoSavedTime" else "草稿已保存",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    AutoSaveStatus.IDLE -> {}
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Preview Mode Toggle
                        IconButton(
                            onClick = { isPreviewMode = !isPreviewMode },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (isPreviewMode) Icons.Default.Edit else Icons.Default.MenuBook,
                                contentDescription = if (isPreviewMode) "切换到编辑" else "排版预览",
                                tint = if (isPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { isFavorite = !isFavorite },
                            modifier = Modifier
                                .testTag("editor_favorite_toggle")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavorite) "取消收藏" else "收藏",
                                tint = if (isFavorite) Color(0xFFE76F51) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                if (rawContent.isNotBlank() || title.isNotBlank() || images.isNotEmpty()) {
                                    activeDraftManager.clearDraft(draftKey)
                                    if (existingEntry == null) {
                                        activeDraftManager.clearNewEntryDraft()
                                    }
                                    onSave(
                                        existingEntry?.id ?: 0L,
                                        title,
                                        rawContent,
                                        selectedMood.emoji,
                                        selectedMood.label,
                                        selectedWeather.emoji,
                                        selectedWeather.label,
                                        temperature,
                                        location,
                                        DiaryEntry.formatImages(images),
                                        audioPath,
                                        audioDurationSec,
                                        selectedCategory,
                                        currentTimestamp,
                                        isFavorite
                                    )
                                }
                            },
                            enabled = rawContent.isNotBlank() || title.isNotBlank() || images.isNotEmpty(),
                            modifier = Modifier.testTag("editor_save_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("保存", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 草稿恢复提示条（若存在上次未保存的草稿）
                    AnimatedVisibility(visible = availableDraft != null) {
                        availableDraft?.let { draft ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "检测到上次未保存的本地草稿",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "保存于 ${draft.formattedFullSavedTime} · 约 ${draft.wordCount} 字",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = {
                                                title = draft.title
                                                contentValue = TextFieldValue(
                                                    text = draft.content,
                                                    selection = TextRange(draft.content.length)
                                                )
                                                selectedMood = AvailableMoods.find { it.emoji == draft.mood } ?: selectedMood
                                                selectedWeather = AvailableWeathers.find { it.emoji == draft.weather } ?: selectedWeather
                                                temperature = draft.temperature
                                                location = draft.location
                                                images = draft.images
                                                selectedCategory = draft.category
                                                isFavorite = draft.isFavorite
                                                currentTimestamp = draft.timestamp
                                                lastAutoSavedTime = draft.formattedSavedTime
                                                autoSaveState = AutoSaveStatus.SAVED
                                                availableDraft = null
                                                hasLocalChanges = true
                                                Toast.makeText(context, "已恢复未保存草稿", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("恢复", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                activeDraftManager.clearDraft(draftKey)
                                                availableDraft = null
                                                Toast.makeText(context, "已忽略草稿", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("忽略", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. 标题输入区
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_input"),
                        placeholder = { Text("为今天起个标题（选填）") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. 一站式日记情境面板（心境、分类与天气地点整合）
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // 心情直接选择
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "今日心情",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "已选：${selectedMood.emoji} ${selectedMood.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AvailableMoods.forEach { mood ->
                                    val isSelected = selectedMood.emoji == mood.emoji
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { selectedMood = mood }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = mood.emoji, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = mood.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 分类与写作灵感入口
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "日记分类",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showInspirationSheet = !showInspirationSheet }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "写作灵感",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (showInspirationSheet) "收起灵感" else "写作灵感",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DefaultCategories.forEach { category ->
                                    val isSelected = selectedCategory == category
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { selectedCategory = category }
                                    ) {
                                        Text(
                                            text = "#$category",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            // 灵感思考展开框
                            if (showInspirationSheet) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "💡 点击题目直接填入思考方向：",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        InspirationalPrompts.take(4).forEach { prompt ->
                                            Text(
                                                text = "• $prompt",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        val newText = if (rawContent.isBlank()) {
                                                            "【$prompt】\n\n"
                                                        } else {
                                                            "$rawContent\n\n【$prompt】\n"
                                                        }
                                                        contentValue = TextFieldValue(newText, TextRange(newText.length))
                                                        showInspirationSheet = false
                                                    }
                                                    .padding(vertical = 3.dp, horizontal = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 天气与地点卡片（折叠/展开）
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { isWeatherCustomExpanded = !isWeatherCustomExpanded }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Text(text = selectedWeather.emoji, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "${selectedWeather.label} · $temperature",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (location.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = location,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isWeatherCustomExpanded) "收起" else "调整地点/天气",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Icon(
                                            imageVector = if (isWeatherCustomExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // 展开的气象与地点详细设置面板
                            AnimatedVisibility(visible = isWeatherCustomExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    RealTimeWeatherLocationSection(
                                        currentLocation = location,
                                        onLocationChange = { location = it },
                                        selectedWeather = selectedWeather.emoji,
                                        selectedWeatherLabel = selectedWeather.label,
                                        temperature = temperature,
                                        onWeatherSelect = { selectedWeather = it },
                                        onTemperatureChange = { temperature = it }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. 正文编辑核心区
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPreviewMode) "📖 排版预览" else "✏️ 正文内容 (支持 Markdown 富文本)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = if (isPreviewMode) "点击右上角可切换回编辑" else "字数: $wordCount",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (isPreviewMode) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (rawContent.isBlank()) {
                                    Text(
                                        text = "暂无正文内容，请切换至编辑模式输入文字或使用富文本工具。",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                } else {
                                    RichTextDocument(content = rawContent)
                                }
                            }
                        }
                    } else {
                        Column {
                            RichTextFormattingToolbar(
                                textFieldValue = contentValue,
                                onValueChange = { contentValue = it }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = contentValue,
                                onValueChange = { contentValue = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 220.dp)
                                    .testTag("editor_content_input"),
                                placeholder = {
                                    Text("写下今天的所见所闻、心情感慨，或是想要倾诉的悄悄话…\n\n可点击上方工具栏快速添加加粗、列表、标题或引用块，或使用 AI 智能排版。")
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 字数与字符统计
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "字数：$wordCount · 字符：$charCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. 照片附件区
                    PhotoAttachmentSection(
                        images = images,
                        onImagesChange = { images = it },
                        onPhotoClick = { previewImageUrl = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. 语音随笔录音区
                    AudioNoteEditorSection(
                        audioPath = audioPath,
                        audioDurationSec = audioDurationSec,
                        onAudioRecorded = { path, dur ->
                            audioPath = path
                            audioDurationSec = dur
                        },
                        onAudioRemoved = {
                            audioPath = ""
                            audioDurationSec = 0
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Writing Assistant Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI 创作与排版助理",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                if (isAiWorking) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "思考中…",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 模板库
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        showTemplatePicker = true
                                    },
                                    label = { Text("📝 模板库") },
                                    enabled = !isAiWorking
                                )

                                // 1. AI 富文本智能排版
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        if (rawContent.isBlank()) {
                                            aiErrorMessage = "请先输入日记草稿，AI 将自动进行优美 Markdown 富文本分段与金句排版"
                                            return@FilterChip
                                        }
                                        isAiWorking = true
                                        aiActionTitle = "🎨 智能排版"
                                        aiGeneratedResult = null
                                        aiExtractedMetadata = null
                                        aiErrorMessage = null
                                        coroutineScope.launch {
                                            val res = aiService.formatAsRichTextDiary(rawContent)
                                            isAiWorking = false
                                            if (res.isSuccess) aiGeneratedResult = res.getOrNull()
                                            else aiErrorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    label = { Text("🎨 智能排版") },
                                    enabled = !isAiWorking
                                )

                                // 2. AI 扩写
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        if (rawContent.isBlank()) {
                                            aiErrorMessage = "请先在下方输入一点关键词或想法草稿，以便 AI 进行扩写"
                                            return@FilterChip
                                        }
                                        isAiWorking = true
                                        aiActionTitle = "✨ 灵感扩写"
                                        aiGeneratedResult = null
                                        aiExtractedMetadata = null
                                        aiErrorMessage = null
                                        coroutineScope.launch {
                                            val res = aiService.expandDiary(rawContent)
                                            isAiWorking = false
                                            if (res.isSuccess) aiGeneratedResult = res.getOrNull()
                                            else aiErrorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    label = { Text("✨ 灵感扩写") },
                                    enabled = !isAiWorking
                                )

                                // 3. 文字润色
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        if (rawContent.isBlank()) {
                                            aiErrorMessage = "请先输入日记内容，以便进行润色"
                                            return@FilterChip
                                        }
                                        isAiWorking = true
                                        aiActionTitle = "📝 文字润色"
                                        aiGeneratedResult = null
                                        aiExtractedMetadata = null
                                        aiErrorMessage = null
                                        coroutineScope.launch {
                                            val res = aiService.polishText(rawContent)
                                            isAiWorking = false
                                            if (res.isSuccess) aiGeneratedResult = res.getOrNull()
                                            else aiErrorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    label = { Text("📝 文字润色") },
                                    enabled = !isAiWorking
                                )

                                // 4. 图文/地点意境生成
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        isAiWorking = true
                                        aiActionTitle = "📸 图文意境生成"
                                        aiGeneratedResult = null
                                        aiExtractedMetadata = null
                                        aiErrorMessage = null
                                        coroutineScope.launch {
                                            val res = aiService.generatePhotoLocationDiary(
                                                location = location,
                                                weather = "${selectedWeather.emoji} ${selectedWeather.label}",
                                                temperature = temperature,
                                                mood = "${selectedMood.emoji} ${selectedMood.label}",
                                                hint = rawContent.ifBlank { title }
                                            )
                                            isAiWorking = false
                                            if (res.isSuccess) aiGeneratedResult = res.getOrNull()
                                            else aiErrorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    label = { Text("📸 图文日记") },
                                    enabled = !isAiWorking
                                )

                                // 5. 待办提取
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        if (rawContent.isBlank()) {
                                            aiErrorMessage = "请先编写日记内容，AI 将自动从中提取行动待办项"
                                            return@FilterChip
                                        }
                                        isAiWorking = true
                                        aiActionTitle = "📋 提取待办"
                                        aiGeneratedResult = null
                                        aiExtractedMetadata = null
                                        aiErrorMessage = null
                                        coroutineScope.launch {
                                            val res = aiService.extractTodosFromDiary(rawContent)
                                            isAiWorking = false
                                            if (res.isSuccess) {
                                                extractedTodosState = res.getOrNull()
                                                extractedTodosSuccess = false
                                            } else {
                                                aiErrorMessage = res.exceptionOrNull()?.message
                                            }
                                        }
                                    },
                                    label = { Text("📋 提取待办") },
                                    enabled = !isAiWorking
                                )

                                // 6. 智能标题与标签
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        if (rawContent.isBlank()) {
                                            aiErrorMessage = "请先输入日记内容，以便提取匹配的标题与情绪标签"
                                            return@FilterChip
                                        }
                                        isAiWorking = true
                                        aiActionTitle = "🏷️ 智能标题与标签"
                                        aiGeneratedResult = null
                                        aiErrorMessage = null
                                        coroutineScope.launch {
                                            val res = aiService.extractMetadata(rawContent)
                                            isAiWorking = false
                                            if (res.isSuccess) aiExtractedMetadata = res.getOrNull()
                                            else aiErrorMessage = res.exceptionOrNull()?.message
                                        }
                                    },
                                    label = { Text("🏷️ 标题与标签") },
                                    enabled = !isAiWorking
                                )
                            }
                        }
                    }

                    // Extracted Todos Card
                    extractedTodosState?.let { todos ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📋 识别出的待办与行动事项：",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    IconButton(
                                        onClick = { extractedTodosState = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                todos.forEach { item ->
                                    Text(
                                        text = "• $item",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                if (extractedTodosSuccess) {
                                    Text(
                                        text = "✅ 已成功存入待办清单！",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            onAddExtractedTodos?.invoke(todos)
                                            extractedTodosSuccess = true
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("一键存入待办清单 (${todos.size} 项)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // AI Result Card
                    aiGeneratedResult?.let { resultText ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$aiActionTitle 生成结果：",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { aiGeneratedResult = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = resultText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val newText = if (rawContent.isBlank()) resultText else "$rawContent\n\n$resultText"
                                            contentValue = TextFieldValue(newText, TextRange(newText.length))
                                            aiGeneratedResult = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("追加到文末", fontSize = 13.sp)
                                    }
                                    Button(
                                        onClick = {
                                            contentValue = TextFieldValue(resultText, TextRange(resultText.length))
                                            aiGeneratedResult = null
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("替换当前正文", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // AI Extracted Metadata Result Card
                    aiExtractedMetadata?.let { meta ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🏷️ 智能分析建议：",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("推荐标题：${meta.recommendedTitle}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text("推荐心情：${meta.moodEmoji} ${meta.moodLabel}", style = MaterialTheme.typography.bodySmall)
                                Text("推荐天气：${meta.weatherEmoji} ${meta.weatherLabel}", style = MaterialTheme.typography.bodySmall)
                                Text("推荐分类：#${meta.category}", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        title = meta.recommendedTitle
                                        selectedCategory = meta.category
                                        AvailableMoods.find { it.label == meta.moodLabel }?.let { selectedMood = it }
                                        AvailableWeathers.find { it.label == meta.weatherLabel }?.let { selectedWeather = it }
                                        aiExtractedMetadata = null
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("一键应用推荐标题与标签", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // AI Error Message
                    aiErrorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC62828),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { aiErrorMessage = null }, modifier = Modifier.size(20.dp)) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = Color(0xFFC62828), modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }

            // 退出防丢确认对话框
            if (showExitConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showExitConfirmDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text("草稿已自动保存", fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "您的编辑内容已自动保存至本地草稿${if (lastAutoSavedTime != null) "（$lastAutoSavedTime）" else ""}。\n\n随时可以回来继续编写，无需担心内容丢失。"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showExitConfirmDialog = false
                                Toast.makeText(context, "草稿已保存在本地", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        ) {
                            Text("保留草稿并退出")
                        }
                    },
                    dismissButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    activeDraftManager.clearDraft(draftKey)
                                    showExitConfirmDialog = false
                                    Toast.makeText(context, "已清空草稿", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("放弃草稿")
                            }
                            OutlinedButton(
                                onClick = { showExitConfirmDialog = false }
                            ) {
                                Text("继续编辑")
                            }
                        }
                    }
                )
            }
        }
    }

    // Fullscreen photo viewer dialog
    previewImageUrl?.let { url ->
        FullScreenImageDialog(
            imageUrl = url,
            onDismiss = { previewImageUrl = null }
        )
    }

    // Journal Template Picker Dialog
    if (showTemplatePicker) {
        JournalTemplateDialog(
            onDismiss = { showTemplatePicker = false },
            onSelectTemplate = { tpl ->
                showTemplatePicker = false
                if (title.isBlank()) {
                    title = tpl.title
                }
                AvailableMoods.find { it.emoji == tpl.defaultMoodEmoji || it.label == tpl.defaultMoodLabel }?.let {
                    selectedMood = it
                }
                if (contentValue.text.isBlank()) {
                    contentValue = TextFieldValue(tpl.templateContent, selection = TextRange(tpl.templateContent.length))
                } else {
                    val newText = contentValue.text + "\n\n" + tpl.templateContent
                    contentValue = TextFieldValue(newText, selection = TextRange(newText.length))
                }
                hasLocalChanges = true
                Toast.makeText(context, "已载入【${tpl.title}】模板", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
