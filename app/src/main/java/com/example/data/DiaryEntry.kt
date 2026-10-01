package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String,
    val mood: String = "😊",
    val moodLabel: String = "开心",
    val weather: String = "☀️",
    val weatherLabel: String = "晴",
    val temperature: String = "",
    val location: String = "",
    val imagesJson: String = "",
    val audioPath: String = "",
    val audioDurationSec: Int = 0,
    val category: String = "日常",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val wordCount: Int = calculateWordCount(content)
) {
    val imageList: List<String>
        get() = parseImages(imagesJson)

    val allImages: List<String>
        get() {
            val list = parseImages(imagesJson).toMutableList()
            val regex = Regex("""!\[.*?\]\((.*?)\)""")
            regex.findAll(content).forEach { match ->
                val url = match.groupValues[1].trim()
                if (url.isNotEmpty() && !list.contains(url)) {
                    list.add(url)
                }
            }
            return list
        }

    val cleanPosterContent: String
        get() {
            return content
                .replace(Regex("""!\[.*?\]\(.*?\)"""), "")
                .replace(Regex("""\[video\]\(.*?\)"""), "")
                .replace(Regex("""\[audio\]\(.*?\)"""), "")
                .replace(Regex("""\n{3,}"""), "\n\n")
                .trim()
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)
            return sdf.format(Date(createdAt))
        }

    val dayOfMonth: String
        get() {
            val sdf = SimpleDateFormat("dd", Locale.getDefault())
            return sdf.format(Date(createdAt))
        }

    val monthYear: String
        get() {
            val sdf = SimpleDateFormat("yyyy.MM", Locale.getDefault())
            return sdf.format(Date(createdAt))
        }

    val dayOfWeek: String
        get() {
            val sdf = SimpleDateFormat("EEEE", Locale.CHINESE)
            return sdf.format(Date(createdAt))
        }

    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(createdAt))
        }

    companion object {
        fun calculateWordCount(text: String): Int {
            if (text.isBlank()) return 0
            var count = 0
            var inWord = false
            for (char in text) {
                if (char.isWhitespace() || !char.isLetterOrDigit()) {
                    inWord = false
                } else if (char.code in 0x4E00..0x9FA5) {
                    count++
                    inWord = false
                } else {
                    if (!inWord) {
                        count++
                        inWord = true
                    }
                }
            }
            return count
        }

        fun formatImages(list: List<String>): String {
            return list.filter { it.isNotBlank() }.joinToString("|||")
        }

        fun parseImages(raw: String): List<String> {
            if (raw.isBlank()) return emptyList()
            return raw.split("|||").filter { it.isNotBlank() }
        }
    }
}

data class MoodItem(
    val emoji: String,
    val label: String,
    val colorHex: Long
)

val AvailableMoods = listOf(
    MoodItem("😊", "开心", 0xFFF4A261),
    MoodItem("😌", "平静", 0xFF81B29A),
    MoodItem("✨", "兴奋", 0xFFE76F51),
    MoodItem("💡", "思考", 0xFF577590),
    MoodItem("🌧️", "低落", 0xFF6D6875),
    MoodItem("⚡", "焦虑", 0xFFE07A5F),
    MoodItem("😴", "疲惫", 0xFF9E8279),
    MoodItem("💖", "感恩", 0xFFDDA15E)
)

data class WeatherItem(
    val emoji: String,
    val label: String,
    val defaultTemp: String = "24°C"
)

val AvailableWeathers = listOf(
    WeatherItem("☀️", "晴朗", "26°C"),
    WeatherItem("🌤️", "晴间多云", "25°C"),
    WeatherItem("⛅", "多云", "23°C"),
    WeatherItem("☁️", "阴天", "20°C"),
    WeatherItem("🌦️", "阵雨", "19°C"),
    WeatherItem("🌧️", "细雨", "18°C"),
    WeatherItem("⛈️", "雷阵雨", "21°C"),
    WeatherItem("❄️", "飞雪", "0°C"),
    WeatherItem("🌨️", "阵雪", "-2°C"),
    WeatherItem("💨", "微风", "22°C"),
    WeatherItem("🌫️", "晨雾", "16°C"),
    WeatherItem("🌙", "晴夜", "19°C"),
    WeatherItem("🌅", "晚霞", "25°C"),
    WeatherItem("🌈", "彩虹", "24°C")
)

val DefaultCategories = listOf(
    "日常",
    "随想",
    "工作",
    "学习",
    "旅行",
    "感悟",
    "美食",
    "梦境"
)

val LocationPresets = listOf(
    "🏠 家中",
    "🏢 办公室",
    "🏫 校园",
    "☕ 咖啡馆",
    "🌳 公园",
    "📚 图书馆",
    "✈️ 旅途中",
    "🍽️ 美食小店",
    "🚶 散步街角",
    "🛋️ 客厅沙发"
)

val TemperaturePresets = listOf(
    "16°C", "20°C", "23°C", "26°C", "28°C", "30°C", "12°C", "5°C"
)

val InspirationalPrompts = listOf(
    "今天最让你感到温暖或开心的小事是什么？",
    "今天学到了什么新的见解或感触？",
    "记录下今天吃过的一顿美味或见到的美景。",
    "今天有没有遇到让你有些困扰的事？你是怎么应对的？",
    "想对今天努力生活的自己说一句什么话？",
    "如果用三个词来总结今天，会是哪三个词？",
    "一件今天值得深深感恩的人或事。"
)
