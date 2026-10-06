package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BackupParseResult
import com.example.data.DiaryBackupManager
import com.example.data.DiaryDraft
import com.example.data.DiaryDraftManager
import com.example.data.DiaryEntry
import com.example.data.DiaryRepository
import com.example.data.ImportReport
import com.example.data.ImportStrategy
import com.example.data.TodoItem
import com.example.data.TodoPriority
import com.example.data.TodoRepository
import com.example.data.TimeCapsuleRepository
import com.example.data.FlashbackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DiaryStats(
    val totalEntries: Int = 0,
    val totalWords: Int = 0,
    val streakDays: Int = 0,
    val moodCounts: Map<String, Int> = emptyMap(),
    val favoriteCount: Int = 0
)

data class EditorState(
    val isOpen: Boolean = false,
    val existingEntry: DiaryEntry? = null,
    val initialPrompt: String? = null,
    val initialLocation: String? = null,
    val initialWeatherEmoji: String? = null,
    val initialWeatherLabel: String? = null,
    val initialTemperature: String? = null,
    val initialTimestamp: Long? = null
)

class DiaryViewModel(
    private val repository: DiaryRepository,
    private val todoRepository: TodoRepository,
    val timeCapsuleRepository: TimeCapsuleRepository? = null,
    val draftManager: DiaryDraftManager? = null
) : ViewModel() {

    // 0: 日记, 1: 探索, 2: 待办清单, 3: 空间与设置
    val currentTab = MutableStateFlow(0)

    // 日记视图模式：0: 时间轴列表, 1: 日历与心情热力图
    val diaryViewMode = MutableStateFlow(0)

    val searchQuery = MutableStateFlow("")
    val selectedMood = MutableStateFlow<String?>(null)
    val selectedCategory = MutableStateFlow<String?>(null)
    val onlyFavorites = MutableStateFlow(false)

    val editorState = MutableStateFlow(EditorState())
    val viewingEntry = MutableStateFlow<DiaryEntry?>(null)
    val exportingPosterEntry = MutableStateFlow<DiaryEntry?>(null)
    val showStatsDialog = MutableStateFlow(false)
    val entryToDelete = MutableStateFlow<DiaryEntry?>(null)
    val showAiSettingsDialog = MutableStateFlow(false)
    val showAiInspirationDialog = MutableStateFlow(false)
    val showBackupDialog = MutableStateFlow(false)
    val showSecuritySettingsDialog = MutableStateFlow(false)
    val showThemeSelectorDialog = MutableStateFlow(false)
    val showTimeCapsuleDialog = MutableStateFlow(false)
    val showEmotionalReportDialog = MutableStateFlow(false)
    val showFootprintExplorerDialog = MutableStateFlow(false)
    val showFlashbackDialog = MutableStateFlow(false)
    val showTemplateDialog = MutableStateFlow(false)
    val showAchievementDialog = MutableStateFlow(false)
    val showBookExportDialog = MutableStateFlow(false)
    val showReminderDialog = MutableStateFlow(false)
    val isImporting = MutableStateFlow(false)
    val importReport = MutableStateFlow<ImportReport?>(null)

    // Todo states
    val todoFilter = MutableStateFlow("ALL") // "ALL", "PENDING", "COMPLETED"

    fun setTab(index: Int) {
        currentTab.value = index
    }

    fun setDiaryViewMode(mode: Int) {
        diaryViewMode.value = mode
    }

    fun openPosterDialog(entry: DiaryEntry) {
        exportingPosterEntry.value = entry
    }

    fun closePosterDialog() {
        exportingPosterEntry.value = null
    }

    fun openThemeSelector() {
        showThemeSelectorDialog.value = true
    }

    fun closeThemeSelector() {
        showThemeSelectorDialog.value = false
    }

    fun openSecuritySettings() {
        showSecuritySettingsDialog.value = true
    }

    fun closeSecuritySettings() {
        showSecuritySettingsDialog.value = false
    }

    fun openAiSettings() {
        showAiSettingsDialog.value = true
    }

    fun closeAiSettings() {
        showAiSettingsDialog.value = false
    }

    fun openAiInspiration() {
        showAiInspirationDialog.value = true
    }

    fun closeAiInspiration() {
        showAiInspirationDialog.value = false
    }

    fun openBackupDialog() {
        showBackupDialog.value = true
        importReport.value = null
    }

    fun closeBackupDialog() {
        showBackupDialog.value = false
        importReport.value = null
    }

    fun openTimeCapsule() {
        showTimeCapsuleDialog.value = true
    }

    fun closeTimeCapsule() {
        showTimeCapsuleDialog.value = false
    }

    fun openEmotionalReport() {
        showEmotionalReportDialog.value = true
    }

    fun closeEmotionalReport() {
        showEmotionalReportDialog.value = false
    }

    fun openFootprintExplorer() {
        showFootprintExplorerDialog.value = true
    }

    fun closeFootprintExplorer() {
        showFootprintExplorerDialog.value = false
    }

    fun openFlashback() {
        showFlashbackDialog.value = true
    }

    fun closeFlashback() {
        showFlashbackDialog.value = false
    }

    fun openTemplateDialog() {
        showTemplateDialog.value = true
    }

    fun closeTemplateDialog() {
        showTemplateDialog.value = false
    }

    fun openAchievements() {
        showAchievementDialog.value = true
    }

    fun closeAchievements() {
        showAchievementDialog.value = false
    }

    fun openBookExport() {
        showBookExportDialog.value = true
    }

    fun closeBookExport() {
        showBookExportDialog.value = false
    }

    fun openReminderDialog() {
        showReminderDialog.value = true
    }

    fun closeReminderDialog() {
        showReminderDialog.value = false
    }

    // All raw entries from database
    val allRawEntries: StateFlow<List<DiaryEntry>> = repository.allEntries.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Number of flashbacks for today
    val flashbackCount: StateFlow<Int> = allRawEntries.map { entries ->
        FlashbackManager.findFlashbacks(entries).size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Filtered entries for display
    val filteredEntries: StateFlow<List<DiaryEntry>> = combine(
        allRawEntries,
        searchQuery,
        selectedMood,
        selectedCategory,
        onlyFavorites
    ) { entries, query, mood, category, favoritesOnly ->
        entries.filter { entry ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                entry.title.contains(query, ignoreCase = true) ||
                    entry.content.contains(query, ignoreCase = true) ||
                    entry.category.contains(query, ignoreCase = true)
            }
            val matchesMood = mood == null || entry.moodLabel == mood || entry.mood == mood
            val matchesCategory = category == null || category == "全部" || entry.category == category
            val matchesFavorite = !favoritesOnly || entry.isFavorite

            matchesQuery && matchesMood && matchesCategory && matchesFavorite
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Todos filtered
    val filteredTodos: StateFlow<List<TodoItem>> = combine(
        todoRepository.allTodos,
        todoFilter
    ) { todos, filter ->
        when (filter) {
            "PENDING" -> todos.filter { !it.isCompleted }
            "COMPLETED" -> todos.filter { it.isCompleted }
            else -> todos
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingTodoCount: StateFlow<Int> = todoRepository.allTodos.map { list ->
        list.count { !it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val completedTodoCount: StateFlow<Int> = todoRepository.allTodos.map { list ->
        list.count { it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val allTodoCount: StateFlow<Int> = todoRepository.allTodos.map { list ->
        list.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Computed overall statistics
    val diaryStats: StateFlow<DiaryStats> = allRawEntries.combine(filteredEntries) { allList, _ ->
        computeStats(allList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiaryStats()
    )

    private fun computeStats(entries: List<DiaryEntry>): DiaryStats {
        if (entries.isEmpty()) return DiaryStats()

        val totalWords = entries.sumOf { it.wordCount }
        val favorites = entries.count { it.isFavorite }
        val moodCounts = entries.groupingBy { it.mood }.eachCount()

        // Calculate continuous streak days
        val dayTimestamps = entries.map { entry ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = entry.createdAt
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }.distinct().sortedDescending()

        var streak = 0
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMillis = todayCal.timeInMillis
        val oneDayMillis = 24 * 60 * 60 * 1000L

        // Check if there is an entry today or yesterday to begin counting streak
        if (dayTimestamps.isNotEmpty()) {
            val mostRecent = dayTimestamps.first()
            var expectedDay = if (mostRecent == todayMillis) todayMillis else todayMillis - oneDayMillis

            for (day in dayTimestamps) {
                if (day == expectedDay) {
                    streak++
                    expectedDay -= oneDayMillis
                } else if (day < expectedDay) {
                    break
                }
            }
        }

        return DiaryStats(
            totalEntries = entries.size,
            totalWords = totalWords,
            streakDays = streak,
            moodCounts = moodCounts,
            favoriteCount = favorites
        )
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onSelectMood(mood: String?) {
        selectedMood.value = if (selectedMood.value == mood) null else mood
    }

    fun onSelectCategory(category: String?) {
        selectedCategory.value = if (selectedCategory.value == category) null else category
    }

    fun toggleFavoritesOnly() {
        onlyFavorites.value = !onlyFavorites.value
    }

    fun clearFilters() {
        searchQuery.value = ""
        selectedMood.value = null
        selectedCategory.value = null
        onlyFavorites.value = false
    }

    fun openNewEntry(
        starterPrompt: String? = null,
        location: String? = null,
        weatherEmoji: String? = null,
        weatherLabel: String? = null,
        temperature: String? = null,
        initialTimestamp: Long? = null
    ) {
        editorState.value = EditorState(
            isOpen = true,
            existingEntry = null,
            initialPrompt = starterPrompt,
            initialLocation = location,
            initialWeatherEmoji = weatherEmoji,
            initialWeatherLabel = weatherLabel,
            initialTemperature = temperature,
            initialTimestamp = initialTimestamp
        )
    }

    fun openEditEntry(entry: DiaryEntry) {
        editorState.value = EditorState(
            isOpen = true,
            existingEntry = entry
        )
    }

    fun closeEditor() {
        editorState.value = EditorState(isOpen = false)
    }

    fun openDetail(entry: DiaryEntry) {
        viewingEntry.value = entry
    }

    fun closeDetail() {
        viewingEntry.value = null
    }

    fun openStats() {
        showStatsDialog.value = true
    }

    fun closeStats() {
        showStatsDialog.value = false
    }

    fun confirmDelete(entry: DiaryEntry) {
        entryToDelete.value = entry
    }

    fun dismissDeleteConfirm() {
        entryToDelete.value = null
    }

    fun executeDelete() {
        entryToDelete.value?.let { entry ->
            viewModelScope.launch {
                repository.delete(entry)
                if (viewingEntry.value?.id == entry.id) {
                    viewingEntry.value = null
                }
                entryToDelete.value = null
            }
        }
    }

    fun saveEntry(
        id: Long,
        title: String,
        content: String,
        mood: String,
        moodLabel: String,
        weather: String,
        weatherLabel: String,
        temperature: String = "",
        location: String = "",
        imagesJson: String = "",
        audioPath: String = "",
        audioDurationSec: Int = 0,
        category: String,
        timestamp: Long,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                id = id,
                title = title.trim(),
                content = content.trim(),
                mood = mood,
                moodLabel = moodLabel,
                weather = weather,
                weatherLabel = weatherLabel,
                temperature = temperature.trim(),
                location = location.trim(),
                imagesJson = imagesJson.trim(),
                audioPath = audioPath.trim(),
                audioDurationSec = audioDurationSec,
                category = category.trim().ifBlank { "日常" },
                createdAt = timestamp,
                updatedAt = System.currentTimeMillis(),
                isFavorite = isFavorite,
                wordCount = DiaryEntry.calculateWordCount(content)
            )
            repository.insertOrUpdate(entry)
            // 成功保存后自动清除对应的本地草稿
            val draftKey = DiaryDraftManager.keyForEntry(id)
            draftManager?.clearDraft(draftKey)
            if (id == 0L) {
                draftManager?.clearNewEntryDraft()
            }
            closeEditor()
            if (viewingEntry.value?.id == id && id != 0L) {
                viewingEntry.value = entry
            }
        }
    }

    fun toggleFavorite(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.toggleFavorite(entry)
            if (viewingEntry.value?.id == entry.id) {
                viewingEntry.value = entry.copy(isFavorite = !entry.isFavorite)
            }
        }
    }

    // --- Todo Actions ---

    fun setTodoFilter(filter: String) {
        todoFilter.value = filter
    }

    fun addTodo(
        title: String,
        priority: String = TodoPriority.NORMAL.name,
        dueDate: Long? = null,
        relatedDiaryId: Long? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val item = TodoItem(
                title = title.trim(),
                priority = priority,
                dueDate = dueDate,
                relatedDiaryId = relatedDiaryId
            )
            todoRepository.insert(item)
        }
    }

    fun addMultipleTodos(
        titles: List<String>,
        priority: String = TodoPriority.NORMAL.name,
        relatedDiaryId: Long? = null
    ) {
        if (titles.isEmpty()) return
        viewModelScope.launch {
            val items = titles.filter { it.isNotBlank() }.map { title ->
                TodoItem(
                    title = title.trim(),
                    priority = priority,
                    relatedDiaryId = relatedDiaryId
                )
            }
            todoRepository.insertAll(items)
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            todoRepository.toggleCompleted(todo)
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            todoRepository.delete(todo)
        }
    }

    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            todoRepository.update(todo)
        }
    }

    // --- Backup & Restore Actions ---

    suspend fun getExportJsonString(): String {
        val list = repository.getAllEntriesSnapshot()
        return DiaryBackupManager.exportToJson(list)
    }

    suspend fun getExportMarkdownString(): String {
        val list = repository.getAllEntriesSnapshot()
        return DiaryBackupManager.exportToMarkdown(list)
    }

    fun parseBackupJson(rawJson: String): BackupParseResult {
        return DiaryBackupManager.parseJson(rawJson)
    }

    fun executeImport(
        entries: List<DiaryEntry>,
        strategy: ImportStrategy,
        onFinished: ((ImportReport) -> Unit)? = null
    ) {
        viewModelScope.launch {
            isImporting.value = true
            try {
                val report = repository.importEntries(entries, strategy)
                importReport.value = report
                onFinished?.invoke(report)
            } finally {
                isImporting.value = false
            }
        }
    }

    fun dismissImportReport() {
        importReport.value = null
    }

    companion object {
        fun provideFactory(
            repository: DiaryRepository,
            todoRepository: TodoRepository,
            timeCapsuleRepository: TimeCapsuleRepository? = null,
            draftManager: DiaryDraftManager? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiaryViewModel(repository, todoRepository, timeCapsuleRepository, draftManager) as T
                }
            }
    }
}
