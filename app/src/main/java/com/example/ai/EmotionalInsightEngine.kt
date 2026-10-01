package com.example.ai

import com.example.data.DiaryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayTrendPoint(
    val dateLabel: String,
    val moodEmoji: String,
    val score: Float, // 1.0f (lowest) ~ 5.0f (highest)
    val entriesCount: Int
)

data class EmotionalReportData(
    val periodTitle: String,
    val totalEntries: Int,
    val totalWords: Int,
    val activeDaysCount: Int,
    val dominantMoodEmoji: String,
    val dominantMoodLabel: String,
    val vitalityScore: Int, // 0 - 100
    val positivePercent: Int,
    val calmPercent: Int,
    val lowPercent: Int,
    val trendPoints: List<DayTrendPoint>,
    val topKeywords: List<String>,
    val aiReportLetter: String = ""
)

object EmotionalInsightEngine {

    private val moodScores = mapOf(
        "✨" to 5.0f,
        "😊" to 4.5f,
        "🎉" to 4.8f,
        "🥰" to 4.7f,
        "😌" to 4.0f,
        "🤔" to 3.5f,
        "☕" to 3.8f,
        "😴" to 2.8f,
        "🌧️" to 2.2f,
        "💔" to 1.8f,
        "⚡" to 2.0f
    )

    private val positiveMoods = setOf("✨", "😊", "🎉", "🥰")
    private val calmMoods = setOf("😌", "🤔", "☕")
    private val lowMoods = setOf("😴", "🌧️", "💔", "⚡")

    fun analyze(entries: List<DiaryEntry>, days: Int): EmotionalReportData {
        val now = System.currentTimeMillis()
        val periodMillis = days * 24 * 60 * 60 * 1000L
        val cutoff = if (days > 0) now - periodMillis else 0L

        val filtered = entries.filter { it.createdAt >= cutoff }.sortedBy { it.createdAt }

        val periodTitle = when (days) {
            7 -> "近7天 · 心绪周报"
            30 -> "近30天 · 月度心绪复盘"
            else -> "全时期 · 心路画卷"
        }

        if (filtered.isEmpty()) {
            return EmotionalReportData(
                periodTitle = periodTitle,
                totalEntries = 0,
                totalWords = 0,
                activeDaysCount = 0,
                dominantMoodEmoji = "🌱",
                dominantMoodLabel = "初萌",
                vitalityScore = 80,
                positivePercent = 50,
                calmPercent = 50,
                lowPercent = 0,
                trendPoints = emptyList(),
                topKeywords = listOf("生活", "初晨", "微光", "记录"),
                aiReportLetter = "这段时间内暂无记录。生活在每一个当下发光，写下今天的第一篇日记，开启专属于你的心绪复盘吧！"
            )
        }

        val totalWords = filtered.sumOf { it.wordCount }

        // Group by day for active days and trend curve
        val dayFormat = SimpleDateFormat("MM.dd", Locale.getDefault())
        val groupedByDay = filtered.groupBy { dayFormat.format(Date(it.createdAt)) }
        val activeDaysCount = groupedByDay.size

        // Mood breakdown
        val moodCounts = filtered.groupingBy { it.mood }.eachCount()
        val dominantMoodEmoji = moodCounts.maxByOrNull { it.value }?.key ?: "😊"
        val dominantMoodLabel = filtered.find { it.mood == dominantMoodEmoji }?.moodLabel ?: "明朗"

        var positiveCount = 0
        var calmCount = 0
        var lowCount = 0

        filtered.forEach {
            when {
                it.mood in positiveMoods -> positiveCount++
                it.mood in calmMoods -> calmCount++
                it.mood in lowMoods -> lowCount++
                else -> calmCount++
            }
        }

        val total = filtered.size
        val positivePercent = ((positiveCount.toFloat() / total) * 100).toInt()
        val calmPercent = ((calmCount.toFloat() / total) * 100).toInt()
        val lowPercent = (100 - positivePercent - calmPercent).coerceAtLeast(0)

        // Vitality score based on positive ratio, consistency, and words
        val avgWords = totalWords / total
        val consistencyBonus = (activeDaysCount.toFloat() / minOf(days.coerceAtLeast(1), 30) * 20).toInt()
        val baseScore = (positivePercent * 0.5f + calmPercent * 0.35f + 20).toInt()
        val vitalityScore = (baseScore + consistencyBonus + (if (avgWords > 100) 10 else 5)).coerceIn(55, 99)

        // Daily trend points
        val trendPoints = groupedByDay.map { (dateStr, dayEntries) ->
            val avgScore = dayEntries.map { moodScores[it.mood] ?: 3.5f }.average().toFloat()
            val dayDominantMood = dayEntries.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key ?: "😊"
            DayTrendPoint(
                dateLabel = dateStr,
                moodEmoji = dayDominantMood,
                score = avgScore,
                entriesCount = dayEntries.size
            )
        }

        // Keywords extraction
        val commonKeywords = listOf(
            "微光", "专注", "治愈", "热爱", "成长", "平静",
            "步履不停", "自我关怀", "生活气息", "思绪沉淀", "笃定", "阳光"
        )
        val extracted = mutableSetOf<String>()
        val combinedText = filtered.joinToString(" ") { "${it.title} ${it.content}" }
        listOf("咖啡", "散步", "阅读", "阳光", "微风", "温暖", "努力", "完成", "朋友", "家人", "夜色", "晨光").forEach {
            if (combinedText.contains(it)) extracted.add(it)
        }
        commonKeywords.forEach {
            if (extracted.size < 6) extracted.add(it)
        }

        val defaultLetter = buildDefaultReflectionLetter(
            periodTitle = periodTitle,
            totalEntries = total,
            dominantLabel = dominantMoodLabel,
            dominantEmoji = dominantMoodEmoji,
            positivePercent = positivePercent,
            vitalityScore = vitalityScore
        )

        return EmotionalReportData(
            periodTitle = periodTitle,
            totalEntries = total,
            totalWords = totalWords,
            activeDaysCount = activeDaysCount,
            dominantMoodEmoji = dominantMoodEmoji,
            dominantMoodLabel = dominantMoodLabel,
            vitalityScore = vitalityScore,
            positivePercent = positivePercent,
            calmPercent = calmPercent,
            lowPercent = lowPercent,
            trendPoints = trendPoints,
            topKeywords = extracted.take(6),
            aiReportLetter = defaultLetter
        )
    }

    private fun buildDefaultReflectionLetter(
        periodTitle: String,
        totalEntries: Int,
        dominantLabel: String,
        dominantEmoji: String,
        positivePercent: Int,
        vitalityScore: Int
    ): String {
        val toneGreeting = when {
            positivePercent >= 60 -> "这段时光里的你，如沐晨光，眼中满是对生活的热爱与欢喜。"
            positivePercent >= 35 -> "这段时光里的你，沉着而有温度，在起伏的生活节奏中守护着内心的安宁。"
            else -> "这段日子或许有一些不易与疲惫，但你依然认真记录下了点滴心声，这就是最有韧性的温柔。"
        }

        val selfCareTip = when {
            vitalityScore >= 85 -> "继续保持这种昂扬从容的心境，抽空奖励自己一杯醇香的饮品或一次悠闲的公园漫步。"
            else -> "给身心留出半小时完全静止的空白，做三次深度腹式呼吸，抱抱那个默默努力的自己。"
        }

        return """
            在【$periodTitle】里，你留存了 $totalEntries 篇真挚的文字记录。
            
            $toneGreeting 你的核心心境是【$dominantEmoji $dominantLabel】，心力指数达到了 $vitalityScore 分。
            
            文字不仅是岁月的容器，更是治愈自我的良方。那些被你写在纸页上的阳光、微风与思绪，都在默默为你沉淀力量。
            
            💡 **心灵关怀指引**：$selfCareTip 愿你步履不停，生活在每一个当下发光。
        """.trimIndent()
    }
}
