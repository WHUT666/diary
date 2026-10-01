package com.example

import com.example.data.DiaryEntry
import com.example.ui.poster.DiaryPosterTheme
import com.example.util.DiaryDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DiarySchemeATest {

    @Test
    fun testHeatmapGridGeneration() {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        val testEntries = listOf(
            DiaryEntry(
                id = 1L,
                title = "春光明媚",
                content = "今天公园的花都开了，心情格外舒畅。",
                mood = "😊",
                moodLabel = "开心",
                weather = "☀️",
                weatherLabel = "晴天",
                createdAt = now,
                updatedAt = now
            ),
            DiaryEntry(
                id = 2L,
                title = "深夜思考",
                content = "静下心来读了一本书，收获满满。",
                mood = "🌿",
                moodLabel = "平静",
                weather = "🌙",
                weatherLabel = "夜晚",
                createdAt = now - 86400000L,
                updatedAt = now - 86400000L
            )
        )

        val heatmapWeeks = DiaryDateUtils.generateHeatmapGrid(testEntries, weeksCount = 12)
        assertEquals(12, heatmapWeeks.size)

        // Each week must have 7 days
        heatmapWeeks.forEach { week ->
            assertEquals(7, week.days.size)
        }

        // Check that days were created
        val totalDays = heatmapWeeks.flatMap { it.days }
        assertEquals(84, totalDays.size)

        val daysWithEntries = totalDays.filter { it.hasEntry }
        assertTrue("At least one day should have entries", daysWithEntries.isNotEmpty())
    }

    @Test
    fun testPosterThemesAvailability() {
        val themes = DiaryPosterTheme.AllThemes
        assertEquals(4, themes.size)
        assertNotNull(DiaryPosterTheme.Parchment)
        assertNotNull(DiaryPosterTheme.Midnight)
        assertNotNull(DiaryPosterTheme.Matcha)
        assertNotNull(DiaryPosterTheme.Vintage)

        // Verify quote and seal text on Parchment
        assertEquals("日日是好日", DiaryPosterTheme.Parchment.sealText)
        assertTrue(DiaryPosterTheme.Parchment.defaultQuote.isNotBlank())
    }

    @Test
    fun testDiaryDateUtilsFormatting() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 17, 10, 30, 0)
        }
        val millis = cal.timeInMillis

        val formattedDate = DiaryDateUtils.toDisplayDate(millis)
        assertTrue(formattedDate.contains("2026") && formattedDate.contains("9") && formattedDate.contains("17"))

        val dayKey = DiaryDateUtils.toDayKey(millis)
        assertEquals("2026-09-17", dayKey)

        val monthKey = DiaryDateUtils.toMonthKey(millis)
        assertEquals("2026-09", monthKey)

        val weekday = DiaryDateUtils.toWeekday(millis)
        assertTrue(weekday.isNotBlank())
    }
}
