package com.example.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PhotoSizeSelectActual
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AvailableMoods
import com.example.data.DiaryEntry
import com.example.util.DiaryDateUtils
import com.example.util.HapticFeedbackUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DiaryCalendarView(
    allEntries: List<DiaryEntry>,
    onOpenDetail: (DiaryEntry) -> Unit,
    onOpenNewEntryForDate: (timestamp: Long) -> Unit,
    onToggleFavorite: (DiaryEntry) -> Unit,
    onExportPoster: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Calendar state: current visible month & year
    var currentYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) } // 0-based

    // Selected date millis (defaults to today)
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Map entries by day key "yyyy-MM-dd"
    val entriesByDay = remember(allEntries) {
        DiaryDateUtils.groupEntriesByDay(allEntries)
    }

    val selectedDayKey = remember(selectedDateMillis) {
        DiaryDateUtils.toDayKey(selectedDateMillis)
    }
    val entriesOnSelectedDate = remember(entriesByDay, selectedDayKey) {
        entriesByDay[selectedDayKey] ?: emptyList()
    }

    // Heatmap grid (12 weeks)
    val heatmapWeeks = remember(allEntries) {
        DiaryDateUtils.generateHeatmapGrid(allEntries, weeksCount = 12)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 1. Monthly Calendar Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calendar_month_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Month Navigation Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currentYear}年 ${currentMonth + 1}月",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // Reset to Today Pill
                        val isCurrentCalendarMonth = remember(currentYear, currentMonth) {
                            val now = Calendar.getInstance()
                            now.get(Calendar.YEAR) == currentYear && now.get(Calendar.MONTH) == currentMonth
                        }
                        if (!isCurrentCalendarMonth) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        HapticFeedbackUtil.lightImpact(context)
                                        val now = Calendar.getInstance()
                                        currentYear = now.get(Calendar.YEAR)
                                        currentMonth = now.get(Calendar.MONTH)
                                        selectedDateMillis = now.timeInMillis
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "回到今天",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Prev / Next Month Buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                HapticFeedbackUtil.lightImpact(context)
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("calendar_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "上一月",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                HapticFeedbackUtil.lightImpact(context)
                                if (currentMonth == 11) {
                                    currentMonth = 0
                                    currentYear += 1
                                } else {
                                    currentMonth += 1
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("calendar_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "下一月",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weekday Headers: 一, 二, 三, 四, 五, 六, 日
                val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    weekdays.forEach { dayLabel ->
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 7-Column Days Grid for the month
                val monthDays = remember(currentYear, currentMonth) {
                    generateDaysForMonth(currentYear, currentMonth)
                }

                // Chunk into weeks of 7
                val weeks = monthDays.chunked(7)
                weeks.forEach { weekList ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        weekList.forEach { calendarDay ->
                            if (calendarDay == null) {
                                Spacer(modifier = Modifier.weight(1f))
                            } else {
                                val dayKey = calendarDay.dayKey
                                val dayEntries = entriesByDay[dayKey] ?: emptyList()
                                val isSelected = dayKey == selectedDayKey

                                CalendarDayCell(
                                    day = calendarDay,
                                    entries = dayEntries,
                                    isSelected = isSelected,
                                    onClick = {
                                        HapticFeedbackUtil.lightImpact(context)
                                        selectedDateMillis = calendarDay.timeMillis
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Mood Heatmap Matrix Card (近12周心情热力图)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mood_heatmap_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "心情热力图",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "近12周记录矩阵",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Total recorded days count
                    val activeDaysCount = remember(heatmapWeeks) {
                        heatmapWeeks.flatMap { it.days }.count { it.hasEntry }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "已记 $activeDaysCount 天",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Heatmap Grid: Horizontal scrollable 7-row matrix
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Weekday Indicators (一, 三, 五, 日)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        listOf("一", "二", "三", "四", "五", "六", "日").forEachIndexed { index, label ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(16.dp)
                            ) {
                                if (index % 2 == 0) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // 12 Columns of weeks
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        heatmapWeeks.forEach { week ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                week.days.forEach { day ->
                                    val isCurrentSelected = day.dayKey == selectedDayKey
                                    val cellColor = if (day.hasEntry && day.dominantMoodColorHex != null) {
                                        Color(day.dominantMoodColorHex)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(cellColor)
                                            .border(
                                                width = if (isCurrentSelected) 1.5.dp else if (day.isToday) 1.dp else 0.dp,
                                                color = if (isCurrentSelected) MaterialTheme.colorScheme.primary else if (day.isToday) MaterialTheme.colorScheme.outline else Color.Transparent,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                HapticFeedbackUtil.lightImpact(context)
                                                selectedDateMillis = day.dateMillis
                                                // Sync calendar month if outside
                                                val cal = Calendar.getInstance().apply { timeInMillis = day.dateMillis }
                                                currentYear = cal.get(Calendar.YEAR)
                                                currentMonth = cal.get(Calendar.MONTH)
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mood Palette Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvailableMoods.forEach { mood ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(mood.colorHex))
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = mood.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Selected Date Feed Header & Entries List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DiaryDateUtils.toDisplayDate(selectedDateMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${DiaryDateUtils.toWeekday(selectedDateMillis)} · 共 ${entriesOnSelectedDate.size} 篇日记",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick "+ 记这一天" Button
            Button(
                onClick = {
                    HapticFeedbackUtil.lightImpact(context)
                    onOpenNewEntryForDate(selectedDateMillis)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("calendar_add_entry_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("记这一天", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Entries List for Selected Date
        if (entriesOnSelectedDate.isEmpty()) {
            // Friendly Empty State Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "这一天还没有记录生活",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "为那一天补上一段心情或难忘的瞬间吧",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            HapticFeedbackUtil.lightImpact(context)
                            onOpenNewEntryForDate(selectedDateMillis)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("calendar_empty_write_button")
                    ) {
                        Text("✏️ 补记生活")
                    }
                }
            }
        } else {
            // Render Diary Cards for this date
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 60.dp)
            ) {
                entriesOnSelectedDate.forEach { entry ->
                    CalendarDiaryCard(
                        entry = entry,
                        onClick = { onOpenDetail(entry) },
                        onToggleFavorite = { onToggleFavorite(entry) },
                        onExportPoster = { onExportPoster(entry) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: MonthDayInfo,
    entries: List<DiaryEntry>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dominantMood = remember(entries) {
        if (entries.isNotEmpty()) {
            val counts = entries.groupingBy { it.mood }.eachCount()
            counts.maxByOrNull { it.value }?.key ?: entries.first().mood
        } else null
    }

    val moodColorHex = remember(dominantMood) {
        AvailableMoods.find { it.emoji == dominantMood }?.colorHex
    }

    val isToday = day.isToday

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    day.isCurrentMonth -> Color.Transparent
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday) 1.5.dp else if (isSelected) 1.dp else 0.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${day.dayOfMonth}",
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.primary
                    day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                }
            )

            // Mood Emoji / Dot Indicator
            if (entries.isNotEmpty() && dominantMood != null) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = dominantMood,
                    fontSize = 11.sp,
                    lineHeight = 11.sp
                )
            } else if (entries.isNotEmpty() && moodColorHex != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(moodColorHex))
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Multi-entries badge
        if (entries.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entries.size}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CalendarDiaryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExportPoster: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("calendar_diary_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Time, Mood, Weather, Category & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.formattedTime,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "${entry.mood} ${entry.moodLabel}", style = MaterialTheme.typography.labelSmall)
                    if (entry.weather.isNotBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${entry.weather} ${entry.weatherLabel}", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = entry.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Poster & Favorite Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onExportPoster,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoSizeSelectActual,
                            contentDescription = "生成海报",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (entry.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (entry.isFavorite) Color(0xFFE76F51) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Title
            if (entry.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Excerpt
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // Photos Thumbnails (if any)
            if (entry.imageList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.imageList.take(3).forEach { imgUri ->
                        AsyncImage(
                            model = imgUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}

data class MonthDayInfo(
    val dayOfMonth: Int,
    val timeMillis: Long,
    val dayKey: String,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)

private fun generateDaysForMonth(year: Int, month: Int): List<MonthDayInfo?> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon... 7=Sat

    // We want Monday as column 0.
    // If Sunday (1), offset is 6. If Mon (2), offset is 0. If Tue (3), offset is 1.
    val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else (firstDayOfWeek - 2)

    val list = mutableListOf<MonthDayInfo?>()
    for (i in 0 until offset) {
        list.add(null)
    }

    val todayCal = Calendar.getInstance()
    val todayKey = DiaryDateUtils.toDayKey(todayCal.timeInMillis)

    for (day in 1..daysInMonth) {
        cal.set(Calendar.DAY_OF_MONTH, day)
        val millis = cal.timeInMillis
        val key = DiaryDateUtils.toDayKey(millis)
        list.add(
            MonthDayInfo(
                dayOfMonth = day,
                timeMillis = millis,
                dayKey = key,
                isCurrentMonth = true,
                isToday = key == todayKey
            )
        )
    }

    // Pad trailing days to complete full weeks
    while (list.size % 7 != 0) {
        list.add(null)
    }

    return list
}
