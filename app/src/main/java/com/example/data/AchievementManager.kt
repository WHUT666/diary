package com.example.data

data class BadgeItem(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val description: String,
    val category: String,
    val isUnlocked: Boolean,
    val currentProgress: Int,
    val targetProgress: Int,
    val unlockedHint: String
) {
    val progressPercent: Float
        get() = (currentProgress.toFloat() / targetProgress.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
}

object AchievementManager {

    fun computeBadges(
        entries: List<DiaryEntry>,
        streakDays: Int,
        capsuleCount: Int = 0
    ): List<BadgeItem> {
        val totalEntries = entries.size
        val totalWords = entries.sumOf { it.wordCount }
        val audioEntries = entries.count { it.audioPath.isNotBlank() }
        val distinctLocations = entries.map { it.location.trim() }.filter { it.isNotBlank() }.distinct().size
        val distinctMoods = entries.map { it.mood }.distinct().size
        val favoriteCount = entries.count { it.isFavorite }

        return listOf(
            BadgeItem(
                id = "first_step",
                title = "初心初启",
                iconEmoji = "🌱",
                description = "写下人生中第一篇日记",
                category = "起步",
                isUnlocked = totalEntries >= 1,
                currentProgress = totalEntries.coerceAtMost(1),
                targetProgress = 1,
                unlockedHint = "万丈高楼，始于此刻"
            ),
            BadgeItem(
                id = "streak_3",
                title = "渐入佳境",
                iconEmoji = "🔥",
                description = "连续记录日记达到 3 天",
                category = "坚持",
                isUnlocked = streakDays >= 3,
                currentProgress = streakDays.coerceAtMost(3),
                targetProgress = 3,
                unlockedHint = "自律之火，悄然点亮"
            ),
            BadgeItem(
                id = "streak_7",
                title = "一周沉淀",
                iconEmoji = "⭐",
                description = "连续记录日记达到 7 天",
                category = "坚持",
                isUnlocked = streakDays >= 7,
                currentProgress = streakDays.coerceAtMost(7),
                targetProgress = 7,
                unlockedHint = "养成习惯，与时光共舞"
            ),
            BadgeItem(
                id = "streak_21",
                title = "习惯成自然",
                iconEmoji = "🏆",
                description = "连续记录日记达到 21 天",
                category = "坚持",
                isUnlocked = streakDays >= 21,
                currentProgress = streakDays.coerceAtMost(21),
                targetProgress = 21,
                unlockedHint = "21天法则：日记已成为生活方式"
            ),
            BadgeItem(
                id = "words_1000",
                title = "初试啼声",
                iconEmoji = "✍️",
                description = "累计书写超过 1,000 字",
                category = "文笔",
                isUnlocked = totalWords >= 1000,
                currentProgress = totalWords.coerceAtMost(1000),
                targetProgress = 1000,
                unlockedHint = "思如泉涌，字字珠玑"
            ),
            BadgeItem(
                id = "words_10000",
                title = "万字长卷",
                iconEmoji = "📜",
                description = "累计书写超过 10,000 字",
                category = "文笔",
                isUnlocked = totalWords >= 10000,
                currentProgress = totalWords.coerceAtMost(10000),
                targetProgress = 10000,
                unlockedHint = "汇聚成长篇生命史诗"
            ),
            BadgeItem(
                id = "audio_soul",
                title = "原声之魅",
                iconEmoji = "🎙️",
                description = "录制并珍藏至少 1 篇语音原声日记",
                category = "多模态",
                isUnlocked = audioEntries >= 1,
                currentProgress = audioEntries.coerceAtMost(1),
                targetProgress = 1,
                unlockedHint = "留住声线里的温度与悸动"
            ),
            BadgeItem(
                id = "time_capsule_keeper",
                title = "时光信使",
                iconEmoji = "💌",
                description = "封存至少 1 颗面向未来的时光胶囊",
                category = "仪式",
                isUnlocked = capsuleCount >= 1,
                currentProgress = capsuleCount.coerceAtMost(1),
                targetProgress = 1,
                unlockedHint = "向未来的自己寄出一束光"
            ),
            BadgeItem(
                id = "world_traveler",
                title = "足迹画卷",
                iconEmoji = "🗺️",
                description = "在至少 3 个不同地点留下日记足迹",
                category = "漫游",
                isUnlocked = distinctLocations >= 3,
                currentProgress = distinctLocations.coerceAtMost(3),
                targetProgress = 3,
                unlockedHint = "天地山河，步步皆是注脚"
            ),
            BadgeItem(
                id = "mood_rainbow",
                title = "心绪彩虹",
                iconEmoji = "🌈",
                description = "体验并记录过全部 6 种心境表情",
                category = "内省",
                isUnlocked = distinctMoods >= 6,
                currentProgress = distinctMoods.coerceAtMost(6),
                targetProgress = 6,
                unlockedHint = "悲欢离合，皆是饱满的人生"
            ),
            BadgeItem(
                id = "treasure_box",
                title = "特别珍藏家",
                iconEmoji = "💎",
                description = "星标珍藏 3 篇最重要的日记",
                category = "品味",
                isUnlocked = favoriteCount >= 3,
                currentProgress = favoriteCount.coerceAtMost(3),
                targetProgress = 3,
                unlockedHint = "生命中最闪耀的晶莹切片"
            )
        )
    }
}
