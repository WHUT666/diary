package com.example.data

data class CityFootprintGroup(
    val cityName: String,
    val iconEmoji: String,
    val entries: List<DiaryEntry>,
    val firstVisitedDate: String,
    val lastVisitedDate: String,
    val distinctLocations: List<String>,
    val photosCount: Int,
    val dominantMood: String
)

data class FootprintOverview(
    val totalCitiesCount: Int,
    val totalLocationsCount: Int,
    val totalFootprintEntries: Int,
    val cityGroups: List<CityFootprintGroup>,
    val allFootprintEntries: List<DiaryEntry>
)

object FootprintAnalyzer {

    private val knownCities = listOf(
        "北京" to "🏛️",
        "上海" to "🏙️",
        "广州" to "🌆",
        "深圳" to "🚀",
        "杭州" to "🍵",
        "成都" to "🐼",
        "武汉" to "🌉",
        "西安" to "🏺",
        "南京" to "🏯",
        "苏州" to "🛶",
        "重庆" to "🌶️",
        "青岛" to "🌊",
        "厦门" to "⛵",
        "昆明" to "🌸",
        "大理" to "🏔️",
        "丽江" to "🏮",
        "拉萨" to "☀️",
        "香港" to "🌃",
        "澳门" to "🎰",
        "台北" to "🧋",
        "东京" to "🗼",
        "京都" to "⛩️",
        "巴黎" to "🥐",
        "纽约" to "🗽",
        "伦敦" to "🕰️"
    )

    fun analyze(entries: List<DiaryEntry>): FootprintOverview {
        val footprintEntries = entries.filter { it.location.isNotBlank() }

        if (footprintEntries.isEmpty()) {
            return FootprintOverview(
                totalCitiesCount = 0,
                totalLocationsCount = 0,
                totalFootprintEntries = 0,
                cityGroups = emptyList(),
                allFootprintEntries = emptyList()
            )
        }

        val allLocations = footprintEntries.map { it.location.trim() }.distinct()

        // Group by City or Distinct Area
        val groupsMap = mutableMapOf<String, MutableList<DiaryEntry>>()
        val groupIcons = mutableMapOf<String, String>()

        footprintEntries.forEach { entry ->
            val loc = entry.location
            var matchedCity: String? = null
            var matchedIcon = "📍"

            for ((city, icon) in knownCities) {
                if (loc.contains(city)) {
                    matchedCity = city
                    matchedIcon = icon
                    break
                }
            }

            val groupKey = matchedCity ?: extractGeneralArea(loc)
            groupsMap.getOrPut(groupKey) { mutableListOf() }.add(entry)
            if (!groupIcons.containsKey(groupKey)) {
                groupIcons[groupKey] = matchedIcon
            }
        }

        val cityGroups = groupsMap.map { (cityName, groupEntries) ->
            val sorted = groupEntries.sortedByDescending { it.createdAt }
            val distinctLocs = sorted.map { it.location }.distinct()
            val photosCount = sorted.sumOf { it.imageList.size }
            val dominantMood = sorted.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key ?: "😊"

            CityFootprintGroup(
                cityName = cityName,
                iconEmoji = groupIcons[cityName] ?: "📍",
                entries = sorted,
                firstVisitedDate = sorted.last().formattedDate,
                lastVisitedDate = sorted.first().formattedDate,
                distinctLocations = distinctLocs,
                photosCount = photosCount,
                dominantMood = dominantMood
            )
        }.sortedByDescending { it.entries.size }

        return FootprintOverview(
            totalCitiesCount = cityGroups.size,
            totalLocationsCount = allLocations.size,
            totalFootprintEntries = footprintEntries.size,
            cityGroups = cityGroups,
            allFootprintEntries = footprintEntries.sortedByDescending { it.createdAt }
        )
    }

    private fun extractGeneralArea(loc: String): String {
        // Strip common prefixes like 🏠, ☕, 📍, 🏢
        val clean = loc.replace(Regex("^[\\p{So}\\p{Sk}\\p{Sm}\\p{Sc}\\s]+"), "").trim()
        val parts = clean.split("·", "-", " ", "市", "区", "省")
        val candidate = parts.firstOrNull { it.isNotBlank() } ?: clean
        return if (candidate.length in 2..8) candidate else "生活角落"
    }
}
