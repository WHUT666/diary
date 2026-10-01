package com.example.data

import java.util.Calendar
import java.util.concurrent.TimeUnit

data class FlashbackItem(
    val entry: DiaryEntry,
    val relationType: FlashbackType,
    val relationTitle: String,
    val timeDistanceDescription: String,
    val daysAgo: Long
)

enum class FlashbackType {
    EXACT_YEAR_ANNIVERSARY, // 1年前、2年前的今天
    EXACT_MONTH_ANNIVERSARY, // 几个月前的同日
    ROUND_DAYS_ANNIVERSARY, // 100天前、300天前
    RANDOM_TREASURE // 曾经的某段珍贵回忆
}

object FlashbackManager {

    /**
     * Finds historical entries that resonate with today's date.
     */
    fun findFlashbacks(allEntries: List<DiaryEntry>, currentMillis: Long = System.currentTimeMillis()): List<FlashbackItem> {
        if (allEntries.isEmpty()) return emptyList()

        val todayCal = Calendar.getInstance().apply {
            timeInMillis = currentMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val todayYear = todayCal.get(Calendar.YEAR)
        val todayMonth = todayCal.get(Calendar.MONTH)
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
        val todayStartMillis = todayCal.timeInMillis

        val entryCal = Calendar.getInstance()
        val results = mutableListOf<FlashbackItem>()

        for (entry in allEntries) {
            entryCal.timeInMillis = entry.createdAt
            val entryStartMillis = Calendar.getInstance().apply {
                timeInMillis = entry.createdAt
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Exclude entries written today
            if (entryStartMillis >= todayStartMillis) continue

            val diffMillis = todayStartMillis - entryStartMillis
            val daysAgo = TimeUnit.MILLISECONDS.toDays(diffMillis)
            if (daysAgo <= 0) continue

            val entryYear = entryCal.get(Calendar.YEAR)
            val entryMonth = entryCal.get(Calendar.MONTH)
            val entryDay = entryCal.get(Calendar.DAY_OF_MONTH)

            val yearDiff = todayYear - entryYear

            if (entryMonth == todayMonth && entryDay == todayDay && yearDiff >= 1) {
                // Exact year anniversary (e.g. 1 year ago today, 2 years ago today)
                results.add(
                    FlashbackItem(
                        entry = entry,
                        relationType = FlashbackType.EXACT_YEAR_ANNIVERSARY,
                        relationTitle = "${yearDiff}年前的今天",
                        timeDistanceDescription = "${yearDiff} 年前 · ${daysAgo} 天时光已逝",
                        daysAgo = daysAgo
                    )
                )
            } else if (entryDay == todayDay && daysAgo >= 25 && daysAgo <= 360) {
                // Exact day of the month from a past month
                val monthDiff = (todayYear - entryYear) * 12 + (todayMonth - entryMonth)
                if (monthDiff in 1..11) {
                    results.add(
                        FlashbackItem(
                            entry = entry,
                            relationType = FlashbackType.EXACT_MONTH_ANNIVERSARY,
                            relationTitle = "${monthDiff}个月前的今天",
                            timeDistanceDescription = "${monthDiff} 个月前 · 昔日同日印记",
                            daysAgo = daysAgo
                        )
                    )
                }
            } else if (daysAgo in listOf(100L, 200L, 300L, 500L, 1000L)) {
                // Round days milestone
                results.add(
                    FlashbackItem(
                        entry = entry,
                        relationType = FlashbackType.ROUND_DAYS_ANNIVERSARY,
                        relationTitle = "整整 ${daysAgo} 天前",
                        timeDistanceDescription = "时光刻度 · ${daysAgo} 天前的回响",
                        daysAgo = daysAgo
                    )
                )
            }
        }

        // If no strict date anniversaries, pick the most favorite or oldest historical entries as treasures
        if (results.isEmpty() && allEntries.size >= 3) {
            val candidate = allEntries.filter { it.createdAt < todayStartMillis - 7 * 86400000L }
                .maxByOrNull { (if (it.isFavorite) 1000 else 0) + it.wordCount }
            if (candidate != null) {
                val diffDays = (todayStartMillis - candidate.createdAt) / 86400000L
                results.add(
                    FlashbackItem(
                        entry = candidate,
                        relationType = FlashbackType.RANDOM_TREASURE,
                        relationTitle = "昔日拾光印记",
                        timeDistanceDescription = "${diffDays} 天前的感悟",
                        daysAgo = diffDays
                    )
                )
            }
        }

        return results.sortedByDescending { it.daysAgo }
    }
}
