package com.example.util

import com.example.data.AvailableMoods
import com.example.data.DiaryEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HeatmapDay(
    val dateMillis: Long,
    val dayKey: String, // "yyyy-MM-dd"
    val dayOfMonth: Int,
    val hasEntry: Boolean,
    val dominantMoodEmoji: String?,
    val dominantMoodColorHex: Long?,
    val entryCount: Int,
    val isToday: Boolean
)

data class HeatmapWeek(
    val days: List<HeatmapDay> // 7 days: Mon to Sun
)

object DiaryDateUtils {

    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("yyyy年M月d日", Locale.CHINESE)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.CHINESE)

    fun toDayKey(timestamp: Long): String {
        return dayFormat.format(Date(timestamp))
    }

    fun toMonthKey(timestamp: Long): String {
        return monthFormat.format(Date(timestamp))
    }

    fun toDisplayDate(timestamp: Long): String {
        return displayDateFormat.format(Date(timestamp))
    }

    fun toWeekday(timestamp: Long): String {
        return weekdayFormat.format(Date(timestamp))
    }

    fun isSameDay(t1: Long, t2: Long): Boolean {
        return toDayKey(t1) == toDayKey(t2)
    }

    fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun groupEntriesByDay(entries: List<DiaryEntry>): Map<String, List<DiaryEntry>> {
        return entries.groupBy { toDayKey(it.createdAt) }
    }

    /**
     * Generates a 12-week (84 days) Mood Heatmap grid leading up to the end of the current week.
     * Each week has 7 days from Monday (index 0) to Sunday (index 6).
     */
    fun generateHeatmapGrid(entries: List<DiaryEntry>, weeksCount: Int = 12): List<HeatmapWeek> {
        val entriesByDay = groupEntriesByDay(entries)
        val todayCal = Calendar.getInstance()
        val todayKey = toDayKey(todayCal.timeInMillis)

        // Find Sunday of current week as grid end
        val endCal = Calendar.getInstance().apply {
            timeInMillis = todayCal.timeInMillis
            // If Sunday, DAY_OF_WEEK is 1; in China Monday is start of week
            val dayOfWeek = get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon... 7=Sat
            val daysUntilSunday = if (dayOfWeek == Calendar.SUNDAY) 0 else (8 - dayOfWeek)
            add(Calendar.DAY_OF_YEAR, daysUntilSunday)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        // Total days: weeksCount * 7
        val totalDays = weeksCount * 7
        val startCal = (endCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -(totalDays - 1))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val allDays = mutableListOf<HeatmapDay>()
        val currCal = startCal.clone() as Calendar

        while (!currCal.after(endCal)) {
            val millis = currCal.timeInMillis
            val key = toDayKey(millis)
            val dayEntries = entriesByDay[key] ?: emptyList()
            val dayOfMonth = currCal.get(Calendar.DAY_OF_MONTH)
            val isToday = key == todayKey

            if (dayEntries.isNotEmpty()) {
                // Find dominant mood
                val moodCounts = dayEntries.groupingBy { it.mood }.eachCount()
                val dominantMood = moodCounts.maxByOrNull { it.value }?.key ?: dayEntries.first().mood
                val moodItem = AvailableMoods.find { it.emoji == dominantMood }
                allDays.add(
                    HeatmapDay(
                        dateMillis = millis,
                        dayKey = key,
                        dayOfMonth = dayOfMonth,
                        hasEntry = true,
                        dominantMoodEmoji = dominantMood,
                        dominantMoodColorHex = moodItem?.colorHex ?: 0xFF81B29A,
                        entryCount = dayEntries.size,
                        isToday = isToday
                    )
                )
            } else {
                allDays.add(
                    HeatmapDay(
                        dateMillis = millis,
                        dayKey = key,
                        dayOfMonth = dayOfMonth,
                        hasEntry = false,
                        dominantMoodEmoji = null,
                        dominantMoodColorHex = null,
                        entryCount = 0,
                        isToday = isToday
                    )
                )
            }
            currCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Chunk into weeks of 7 days
        return allDays.chunked(7).map { HeatmapWeek(it) }
    }
}
