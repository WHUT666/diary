package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.example.ui.components.IosColors
import com.example.ui.components.IosSearchBar
import com.example.ui.components.IosSegmentedControl
import com.example.ui.explore.ExploreScreen
import com.example.ui.settings.SettingsScreen
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoSizeSelectActual
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiConfigManager
import com.example.ai.AiService
import com.example.data.AvailableMoods
import com.example.data.DefaultCategories
import com.example.data.DiaryEntry
import com.example.ui.calendar.DiaryCalendarView
import com.example.ui.capsule.TimeCapsuleDialog
import com.example.ui.components.FrostedGlassBar
import com.example.ui.components.SwipeableActionItem
import com.example.ui.footprint.FootprintExplorerDialog
import com.example.ui.poster.DiaryPosterDialog
import com.example.ui.report.EmotionalReportDialog
import com.example.ui.flashback.MemoryFlashbackDialog
import com.example.ui.template.JournalTemplateDialog
import com.example.ui.badge.AchievementBadgesDialog
import com.example.ui.export.DiaryBookExportDialog
import com.example.ui.reminder.DailyReminderDialog
import com.example.ui.theme.AppThemeManager
import com.example.util.HapticFeedbackUtil
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    aiService: AiService,
    configManager: AiConfigManager,
    securityLockManager: com.example.security.SecurityLockManager? = null,
    themeManager: AppThemeManager? = null,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val diaryViewMode by viewModel.diaryViewMode.collectAsStateWithLifecycle()
    val allRawEntries by viewModel.allRawEntries.collectAsStateWithLifecycle()
    val exportingPosterEntry by viewModel.exportingPosterEntry.collectAsStateWithLifecycle()
    val entries by viewModel.filteredEntries.collectAsStateWithLifecycle()
    val stats by viewModel.diaryStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMood by viewModel.selectedMood.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()
    val pendingTodoCount by viewModel.pendingTodoCount.collectAsStateWithLifecycle()

    val editorState by viewModel.editorState.collectAsStateWithLifecycle()
    val viewingEntry by viewModel.viewingEntry.collectAsStateWithLifecycle()
    val showStatsDialog by viewModel.showStatsDialog.collectAsStateWithLifecycle()
    val entryToDelete by viewModel.entryToDelete.collectAsStateWithLifecycle()
    val showAiSettings by viewModel.showAiSettingsDialog.collectAsStateWithLifecycle()
    val showAiInspiration by viewModel.showAiInspirationDialog.collectAsStateWithLifecycle()
    val showBackupDialog by viewModel.showBackupDialog.collectAsStateWithLifecycle()
    val showSecuritySettings by viewModel.showSecuritySettingsDialog.collectAsStateWithLifecycle()
    val showThemeSelector by viewModel.showThemeSelectorDialog.collectAsStateWithLifecycle()
    val showTimeCapsuleDialog by viewModel.showTimeCapsuleDialog.collectAsStateWithLifecycle()
    val showEmotionalReportDialog by viewModel.showEmotionalReportDialog.collectAsStateWithLifecycle()
    val showFootprintExplorerDialog by viewModel.showFootprintExplorerDialog.collectAsStateWithLifecycle()
    val showFlashbackDialog by viewModel.showFlashbackDialog.collectAsStateWithLifecycle()
    val showTemplateDialog by viewModel.showTemplateDialog.collectAsStateWithLifecycle()
    val showAchievementDialog by viewModel.showAchievementDialog.collectAsStateWithLifecycle()
    val showBookExportDialog by viewModel.showBookExportDialog.collectAsStateWithLifecycle()
    val showReminderDialog by viewModel.showReminderDialog.collectAsStateWithLifecycle()
    val flashbackCount by viewModel.flashbackCount.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val todayDateFormatted = remember {
        val sdf = SimpleDateFormat("yyyy年MM月dd日 · EEEE", Locale.CHINESE)
        sdf.format(Date())
    }

    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                val navItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = IosColors.SystemGray,
                    unselectedTextColor = IosColors.SystemGray,
                    indicatorColor = Color.Transparent
                )

                // Tab 0: 日记
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = {
                        HapticFeedbackUtil.lightImpact(context)
                        viewModel.setTab(0)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "日记"
                        )
                    },
                    label = { Text("日记", fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.SemiBold else FontWeight.Normal) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("tab_diary")
                )

                // Tab 1: 探索
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = {
                        HapticFeedbackUtil.lightImpact(context)
                        viewModel.setTab(1)
                    },
                    icon = {
                        BadgedBox(badge = {
                            if (flashbackCount > 0) {
                                Badge(
                                    containerColor = IosColors.SystemRed,
                                    contentColor = Color.White
                                ) { Text("$flashbackCount", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "探索"
                            )
                        }
                    },
                    label = { Text("探索", fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.SemiBold else FontWeight.Normal) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("tab_explore")
                )

                // Tab 2: 待办
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = {
                        HapticFeedbackUtil.lightImpact(context)
                        viewModel.setTab(2)
                    },
                    icon = {
                        BadgedBox(badge = {
                            if (pendingTodoCount > 0) {
                                Badge(
                                    containerColor = IosColors.SystemRed,
                                    contentColor = Color.White
                                ) { Text("$pendingTodoCount", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "待办"
                            )
                        }
                    },
                    label = { Text("待办", fontSize = 11.sp, fontWeight = if (currentTab == 2) FontWeight.SemiBold else FontWeight.Normal) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("tab_todo")
                )

                // Tab 3: 设置与空间
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = {
                        HapticFeedbackUtil.lightImpact(context)
                        viewModel.setTab(3)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "设置"
                        )
                    },
                    label = { Text("设置", fontSize = 11.sp, fontWeight = if (currentTab == 3) FontWeight.SemiBold else FontWeight.Normal) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0 && diaryViewMode == 0 && entries.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 5.dp,
                    border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .testTag("new_diary_fab")
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            HapticFeedbackUtil.lightImpact(context)
                            viewModel.openNewEntry()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "写日记",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.2.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentTab,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "main_screen_tab_crossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    1 -> {
                        // 探索与时光专区 (Explore Tab)
                        ExploreScreen(
                            entries = allRawEntries,
                            streakDays = stats.streakDays,
                            onOpenFlashback = { viewModel.openFlashback() },
                            onOpenTimeCapsule = { viewModel.openTimeCapsule() },
                            onOpenEmotionalReport = { viewModel.openEmotionalReport() },
                            onOpenFootprint = { viewModel.openFootprintExplorer() },
                            onOpenTemplates = { viewModel.openTemplateDialog() },
                            onOpenInspiration = { viewModel.openAiInspiration() },
                            onOpenBadges = { viewModel.openAchievements() },
                            onOpenBookExport = { viewModel.openBookExport() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    2 -> {
                        // 待办清单专区 (Todo Tab)
                        TodoScreen(
                            viewModel = viewModel,
                            aiService = aiService,
                            onOpenDiaryEditorWithPrompt = { prompt ->
                                viewModel.openNewEntry(prompt)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    3 -> {
                        // 空间与设置中心 (Settings Tab)
                        SettingsScreen(
                            entries = allRawEntries,
                            stats = stats,
                            themeManager = themeManager ?: AppThemeManager(context),
                            securityLockManager = securityLockManager ?: com.example.security.SecurityLockManager(context),
                            onOpenThemeSelector = { viewModel.openThemeSelector() },
                            onOpenSecuritySettings = { viewModel.openSecuritySettings() },
                            onOpenReminderDialog = { viewModel.openReminderDialog() },
                            onOpenBackupDialog = { viewModel.openBackupDialog() },
                            onOpenBookExportDialog = { viewModel.openBookExport() },
                            onOpenAiSettings = { viewModel.openAiSettings() },
                            onOpenStats = { viewModel.openStats() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        // 0: 日记
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Apple Large Title Header - Crisp, expansive & unhurried
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = todayDateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "日记",
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = (-0.5).sp
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 打卡天数胶囊 (轻触查看全景统计)
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                        modifier = Modifier
                                            .testTag("stats_pill_button")
                                            .clip(RoundedCornerShape(18.dp))
                                            .clickable {
                                                HapticFeedbackUtil.lightImpact(context)
                                                viewModel.openStats()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalFireDepartment,
                                                contentDescription = "打卡天数",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${stats.streakDays}天",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    // 灵感驿站按钮
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                HapticFeedbackUtil.lightImpact(context)
                                                viewModel.openAiInspiration()
                                            }
                                            .testTag("inspiration_station_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "AI 灵感驿站",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }

                                    // 搜索展开切换
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSearchExpanded || searchQuery.isNotBlank()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                HapticFeedbackUtil.lightImpact(context)
                                                isSearchExpanded = !isSearchExpanded
                                            }
                                            .testTag("search_toggle_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "搜索日记",
                                                tint = if (isSearchExpanded || searchQuery.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // iOS Segmented Control: [时间轴] vs [日历与心情]
                            IosSegmentedControl(
                                items = listOf(0, 1),
                                selectedItem = diaryViewMode,
                                onItemSelected = { mode ->
                                    viewModel.setDiaryViewMode(mode)
                                },
                                itemLabel = { if (it == 0) "时间轴" else "日历与心情" },
                                itemIcon = { if (it == 0) Icons.Default.ViewAgenda else Icons.Default.CalendarMonth },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                    .testTag(if (diaryViewMode == 0) "view_mode_timeline" else "view_mode_calendar")
                            )

                            // 搜索框 (可折叠动画)
                            AnimatedVisibility(
                                visible = isSearchExpanded || searchQuery.isNotBlank(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IosSearchBar(
                                    query = searchQuery,
                                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                                    onClear = { viewModel.onSearchQueryChange("") },
                                    placeholder = "搜索日记标题、正文、心情或标签…",
                                    onCancel = {
                                        isSearchExpanded = false
                                        viewModel.onSearchQueryChange("")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                        .testTag("search_input")
                                )
                            }

                if (diaryViewMode == 1) {
                    // Calendar & Heatmap View
                    DiaryCalendarView(
                        allEntries = allRawEntries,
                        onOpenDetail = { viewModel.openDetail(it) },
                        onOpenNewEntryForDate = { timestamp ->
                            viewModel.openNewEntry(initialTimestamp = timestamp)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onExportPoster = { viewModel.openPosterDialog(it) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Timeline List View

                // 那年今日 历史回响温暖提醒条
                if (flashbackCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                HapticFeedbackUtil.lightImpact(context)
                                viewModel.openFlashback()
                            }
                            .testTag("flashback_banner_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text("🕰️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "那年今日 · 时光闪回",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "发现了 $flashbackCount 篇历史同期的珍贵回忆，轻触重温岁月回响",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Filter Chips Horizontal Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Favorites filter chip
                    FilterChip(
                        selected = onlyFavorites,
                        onClick = { viewModel.toggleFavoritesOnly() },
                        label = { Text("特别珍藏") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (onlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (onlyFavorites) Color(0xFFE76F51) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("filter_favorites")
                    )

                    // Category Chips
                    DefaultCategories.take(5).forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onSelectCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }

                    // Mood Chips
                    AvailableMoods.take(4).forEach { mood ->
                        val isSelected = selectedMood == mood.label
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onSelectMood(mood.label) },
                            label = { Text("${mood.emoji} ${mood.label}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        )
                    }
                }

                // Real-Time Local Weather Banner (Free API + 1-Tap Refresh)
                RealTimeWeatherHomeBanner(
                    onOpenEditor = { loc, weatherEmoji, weatherLabel, temp ->
                        viewModel.openNewEntry(
                            location = loc,
                            weatherEmoji = weatherEmoji,
                            weatherLabel = weatherLabel,
                            temperature = temp
                        )
                    }
                )

                // Timeline Entries List or Empty State
                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (searchQuery.isNotBlank() || selectedMood != null || selectedCategory != null || onlyFavorites) {
                                Text(
                                    text = "未找到符合条件的日记",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "尝试换一个关键词或清除筛选条件",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.clearFilters() }) {
                                    Text("清除所有筛选")
                                }
                            } else {
                                Text(
                                    text = "开启生活的美好记录",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "每一天都值得被温柔以待，写下今天的所见所思吧！",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ExtendedFloatingActionButton(
                                        onClick = { viewModel.openAiInspiration() },
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("灵感驿站", fontWeight = FontWeight.Bold)
                                    }
                                    ExtendedFloatingActionButton(
                                        onClick = { viewModel.openNewEntry("今天最值得记录的事…") },
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("写新日记", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("diary_entries_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            SwipeableActionItem(
                                onDelete = { viewModel.confirmDelete(entry) },
                                onPin = { viewModel.toggleFavorite(entry) },
                                isPinned = entry.isFavorite,
                                pinLabel = if (entry.isFavorite) "取消珍藏" else "特别珍藏",
                                deleteLabel = "删除日记"
                            ) {
                                DiaryCard(
                                    entry = entry,
                                    onClick = {
                                        HapticFeedbackUtil.lightImpact(context)
                                        viewModel.openDetail(entry)
                                    },
                                    onToggleFavorite = {
                                        HapticFeedbackUtil.lightImpact(context)
                                        viewModel.toggleFavorite(entry)
                                    },
                                    onExportPoster = {
                                        viewModel.openPosterDialog(entry)
                                    }
                                )
                            }
                        }
                    }
                }
                }
            }
            }
        }

    // Editor Dialog
    if (editorState.isOpen) {
        DiaryEditorDialog(
            existingEntry = editorState.existingEntry,
            initialPrompt = editorState.initialPrompt,
            initialLocation = editorState.initialLocation,
            initialWeatherEmoji = editorState.initialWeatherEmoji,
            initialWeatherLabel = editorState.initialWeatherLabel,
            initialTemperature = editorState.initialTemperature,
            initialTimestamp = editorState.initialTimestamp,
            draftManager = viewModel.draftManager,
            aiService = aiService,
            onDismiss = { viewModel.closeEditor() },
            onAddExtractedTodos = { todos ->
                viewModel.addMultipleTodos(todos)
            },
            onSave = { id, title, content, mood, moodLabel, weather, weatherLabel, temperature, location, imagesJson, audioPath, audioDurationSec, category, timestamp, isFavorite ->
                viewModel.saveEntry(
                    id = id,
                    title = title,
                    content = content,
                    mood = mood,
                    moodLabel = moodLabel,
                    weather = weather,
                    weatherLabel = weatherLabel,
                    temperature = temperature,
                    location = location,
                    imagesJson = imagesJson,
                    audioPath = audioPath,
                    audioDurationSec = audioDurationSec,
                    category = category,
                    timestamp = timestamp,
                    isFavorite = isFavorite
                )
            }
        )
    }

    // Reading Detail Dialog
    viewingEntry?.let { entry ->
        DiaryDetailDialog(
            entry = entry,
            aiService = aiService,
            onDismiss = { viewModel.closeDetail() },
            onEdit = {
                viewModel.closeDetail()
                viewModel.openEditEntry(it)
            },
            onDelete = { viewModel.confirmDelete(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onAddTodos = { todos, diaryId ->
                viewModel.addMultipleTodos(todos, relatedDiaryId = diaryId)
            },
            onExportPoster = { viewModel.openPosterDialog(it) }
        )
    }

    // Poster Export Dialog
    exportingPosterEntry?.let { posterEntry ->
        DiaryPosterDialog(
            entry = posterEntry,
            onDismiss = { viewModel.closePosterDialog() }
        )
    }

    // Stats Dialog
    if (showStatsDialog) {
        DiaryStatsDialog(
            stats = stats,
            onDismiss = { viewModel.closeStats() },
            onOpenBackup = { viewModel.openBackupDialog() }
        )
    }

    // AI Settings Dialog
    if (showAiSettings) {
        AiSettingsDialog(
            configManager = configManager,
            aiService = aiService,
            onDismiss = { viewModel.closeAiSettings() },
            onConfigSaved = { /* automatically used by next call */ }
        )
    }

    // AI Inspiration Station Dialog
    if (showAiInspiration) {
        AiInspirationStationDialog(
            aiService = aiService,
            onDismiss = { viewModel.closeAiInspiration() },
            onStartNewDiary = { starter ->
                viewModel.openNewEntry(starter)
            }
        )
    }

    // Data Backup and Restore Dialog
    if (showBackupDialog) {
        DiaryBackupDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeBackupDialog() }
        )
    }

    // Privacy & Security Lock Settings Dialog
    if (showSecuritySettings && securityLockManager != null) {
        SecuritySettingsDialog(
            securityLockManager = securityLockManager,
            onDismiss = { viewModel.closeSecuritySettings() },
            onLockStateChanged = { /* state triggers reactive redraw */ }
        )
    }

    // iOS Theme Selector & Style Customizer Dialog
    if (showThemeSelector && themeManager != null) {
        ThemeSelectorDialog(
            themeManager = themeManager,
            onDismiss = { viewModel.closeThemeSelector() }
        )
    }

    // Time Capsule Dialog (Scheme B)
    if (showTimeCapsuleDialog && viewModel.timeCapsuleRepository != null) {
        TimeCapsuleDialog(
            repository = viewModel.timeCapsuleRepository,
            onDismiss = { viewModel.closeTimeCapsule() }
        )
    }

    // AI Emotional Insights Report Dialog
    if (showEmotionalReportDialog) {
        EmotionalReportDialog(
            entries = allRawEntries,
            aiService = aiService,
            onDismiss = { viewModel.closeEmotionalReport() }
        )
    }

    // Footprint Explorer Dialog
    if (showFootprintExplorerDialog) {
        FootprintExplorerDialog(
            entries = allRawEntries,
            onDismiss = { viewModel.closeFootprintExplorer() },
            onSelectEntry = { viewModel.openDetail(it) },
            onWriteAtLocation = { loc ->
                viewModel.openNewEntry(location = loc)
            }
        )
    }

    // Scheme C: Memory Flashbacks (那年今日 · 时光闪回)
    if (showFlashbackDialog) {
        MemoryFlashbackDialog(
            allEntries = allRawEntries,
            aiService = aiService,
            onDismiss = { viewModel.closeFlashback() },
            onOpenEntryDetail = { viewModel.openDetail(it) },
            onWriteResponseToday = { quote ->
                viewModel.closeFlashback()
                viewModel.openNewEntry(starterPrompt = quote)
            }
        )
    }

    // Scheme C: Journal Structured Templates (结构化日记模板库)
    if (showTemplateDialog) {
        JournalTemplateDialog(
            onDismiss = { viewModel.closeTemplateDialog() },
            onSelectTemplate = { tpl ->
                viewModel.closeTemplateDialog()
                viewModel.openNewEntry(starterPrompt = tpl.templateContent)
            }
        )
    }

    // Scheme C: Achievement Badges (写作里程碑与勋章殿堂)
    if (showAchievementDialog) {
        AchievementBadgesDialog(
            entries = allRawEntries,
            streakDays = stats.streakDays,
            capsuleCount = 0,
            onDismiss = { viewModel.closeAchievements() }
        )
    }

    // Scheme C: Book Export (Markdown & TXT 电子书册导出)
    if (showBookExportDialog) {
        DiaryBookExportDialog(
            allEntries = allRawEntries,
            onDismiss = { viewModel.closeBookExport() }
        )
    }

    // Scheme C: Daily Gentle Reminders (每日写作温情提醒)
    if (showReminderDialog) {
        DailyReminderDialog(
            onDismiss = { viewModel.closeReminderDialog() }
        )
    }

    // Delete Confirmation Dialog
    entryToDelete?.let { entry ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirm() },
            title = { Text("确认删除日记？") },
            text = {
                Text(
                    if (entry.title.isNotBlank()) "是否确认删除《${entry.title}》？删除后将无法恢复。"
                    else "是否确认删除这篇日记？删除后将无法恢复。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.executeDelete() },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDeleteConfirm() },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("取消")
                }
            }
        )
    }
        }
    }
}
}

@Composable
fun DiaryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExportPoster: () -> Unit = {}
) {
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
            .clickable(onClick = onClick)
            .testTag("diary_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Date Stamp, Mood & Category Capsules + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Stamp Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "${entry.dayOfMonth}日 · ${entry.monthYear}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Mood Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = "${entry.mood} ${entry.moodLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Category Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "#${entry.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Poster Button
                    IconButton(
                        onClick = onExportPoster,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("card_poster_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoSizeSelectActual,
                            contentDescription = "生成海报",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("card_favorite_${entry.id}")
                    ) {
                        Icon(
                            imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (entry.isFavorite) "取消收藏" else "收藏",
                            tint = if (entry.isFavorite) Color(0xFFE76F51) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title (if present)
            if (entry.title.isNotBlank()) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Photo Thumbnails Row (if any)
            val cardImages = entry.imageList
            if (cardImages.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cardImages.take(3).forEach { imgUri ->
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = imgUri,
                                contentDescription = "照片缩略图",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    if (cardImages.size > 3) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${cardImages.size - 3}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Content excerpt (clean plain string preview)
            val cleanSnippet = remember(entry.content) {
                entry.content
                    .replace(Regex("(?m)^#+\\s*"), "")
                    .replace(Regex("(?m)^>\\s*"), "")
                    .replace(Regex("(?m)^[•\\-*]\\s*"), "")
                    .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
                    .replace(Regex("\\*(.*?)\\*"), "$1")
                    .replace(Regex("~~(.*?)~~"), "$1")
                    .replace(Regex("`{1,3}(.*?)`{1,3}"), "$1")
                    .trim()
            }

            Text(
                text = cleanSnippet,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Footer: Time, Weather, Location & Word count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time, Weather & Location Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = entry.formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = " · ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "${entry.weather} ${entry.weatherLabel}${if (entry.temperature.isNotBlank()) " ${entry.temperature}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    if (entry.location.isNotBlank()) {
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = entry.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${entry.wordCount} 字",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                    if (entry.audioPath.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "🎙️ ${entry.audioDurationSec}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}
