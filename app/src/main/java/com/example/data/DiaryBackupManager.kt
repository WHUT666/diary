package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ImportStrategy(val label: String, val description: String) {
    SMART_MERGE("智能合并 (推荐)", "自动比对已有日记，仅导入未重复的新篇目，安全保留现有数据"),
    APPEND_ALL("全部追加", "将备份中的所有日记作为新记录追加写入，不覆盖现有内容"),
    OVERWRITE_ALL("清空并覆盖", "先清空当前数据库中的所有日记，再完整恢复该备份数据")
}

data class BackupMetadata(
    val version: Int = 1,
    val appName: String = "AI智能日记",
    val exportedAt: Long = System.currentTimeMillis(),
    val exportedAtFormatted: String = "",
    val entryCount: Int = 0,
    val totalWords: Int = 0,
    val earliestDate: String = "",
    val latestDate: String = ""
)

data class BackupParseResult(
    val success: Boolean,
    val metadata: BackupMetadata? = null,
    val entries: List<DiaryEntry> = emptyList(),
    val errorMessage: String? = null
)

data class ImportReport(
    val totalInBackup: Int,
    val newlyInserted: Int,
    val skippedDuplicates: Int,
    val totalNow: Int
)

object DiaryBackupManager {

    private val sdfDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val sdfDate = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE)

    /**
     * 将日记列表导出为格式化 JSON 备份数据（带有应用元信息与缩进）
     */
    fun exportToJson(entries: List<DiaryEntry>): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        root.put("version", 1)
        root.put("appName", "AI智能日记")
        root.put("backupType", "AI_DIARY_FULL_BACKUP")
        root.put("exportedAt", now)
        root.put("exportedAtFormatted", sdfDateTime.format(Date(now)))
        root.put("entryCount", entries.size)

        val totalWords = entries.sumOf { it.wordCount }
        val favCount = entries.count { it.isFavorite }
        val statsObj = JSONObject().apply {
            put("totalWords", totalWords)
            put("favoriteCount", favCount)
        }
        root.put("stats", statsObj)

        val entriesArray = JSONArray()
        for (entry in entries) {
            val item = JSONObject().apply {
                put("id", entry.id)
                put("title", entry.title)
                put("content", entry.content)
                put("mood", entry.mood)
                put("moodLabel", entry.moodLabel)
                put("weather", entry.weather)
                put("weatherLabel", entry.weatherLabel)
                put("temperature", entry.temperature)
                put("location", entry.location)
                put("category", entry.category)
                put("createdAt", entry.createdAt)
                put("createdAtFormatted", sdfDateTime.format(Date(entry.createdAt)))
                put("updatedAt", entry.updatedAt)
                put("isFavorite", entry.isFavorite)
                put("wordCount", entry.wordCount)
                put("imagesJson", entry.imagesJson)

                val imageArray = JSONArray()
                entry.imageList.forEach { imageArray.put(it) }
                put("images", imageArray)
            }
            entriesArray.put(item)
        }
        root.put("entries", entriesArray)

        return root.toString(2)
    }

    /**
     * 将日记列表导出为优美排版的 Markdown 归档文本
     */
    fun exportToMarkdown(entries: List<DiaryEntry>): String {
        val nowFormatted = sdfDateTime.format(Date())
        return buildString {
            append("# 📖 我的日记本 · 完整归档备份\n\n")
            append("> **导出时间**：$nowFormatted  \n")
            append("> **日记总篇数**：${entries.size} 篇  \n")
            append("> **累计字数**：${entries.sumOf { it.wordCount }} 字\n\n")
            append("---\n\n")

            if (entries.isEmpty()) {
                append("_当前尚无日记记录。_\n")
            } else {
                entries.sortedByDescending { it.createdAt }.forEachIndexed { index, entry ->
                    append("## ${index + 1}. ")
                    if (entry.title.isNotBlank()) {
                        append(entry.title)
                    } else {
                        append("无题日记")
                    }
                    append("\n\n")

                    append("- **日期时间**：${entry.formattedDate} ${entry.dayOfWeek} ${entry.formattedTime}\n")
                    append("- **心情状态**：${entry.mood} ${entry.moodLabel}\n")
                    val weatherDesc = buildString {
                        append(entry.weather).append(" ").append(entry.weatherLabel)
                        if (entry.temperature.isNotBlank()) {
                            append(" (").append(entry.temperature).append(")")
                        }
                    }
                    append("- **天气**：$weatherDesc\n")
                    if (entry.location.isNotBlank()) {
                        append("- **地点**：📍 ${entry.location}\n")
                    }
                    append("- **分类**：#${entry.category}")
                    if (entry.isFavorite) {
                        append(" · ⭐ 星标收藏")
                    }
                    append(" · 字数: ${entry.wordCount}\n\n")

                    // 照片列表
                    val images = entry.imageList
                    if (images.isNotEmpty()) {
                        append("**附带照片（${images.size} 张）**：\n")
                        images.forEachIndexed { imgIdx, uri ->
                            append("- 照片 ${imgIdx + 1}: `$uri`\n")
                        }
                        append("\n")
                    }

                    // 正文
                    append("### 正文内容\n\n")
                    append(entry.content.trim())
                    append("\n\n---\n\n")
                }
            }
        }
    }

    /**
     * 单篇日记导出为 Markdown
     */
    fun exportSingleEntryToMarkdown(entry: DiaryEntry): String {
        return buildString {
            if (entry.title.isNotBlank()) {
                append("# ${entry.title}\n\n")
            } else {
                append("# ${entry.formattedDate} 日记\n\n")
            }
            append("> **时间**：${entry.formattedDate} ${entry.dayOfWeek} ${entry.formattedTime}  \n")
            append("> **心情**：${entry.mood} ${entry.moodLabel}  \n")
            append("> **天气**：${entry.weather} ${entry.weatherLabel}${if (entry.temperature.isNotBlank()) " (${entry.temperature})" else ""}  \n")
            if (entry.location.isNotBlank()) {
                append("> **地点**：📍 ${entry.location}  \n")
            }
            append("> **分类**：#${entry.category}  \n")
            append("> **字数**：${entry.wordCount} 字\n\n")
            append("---\n\n")
            append(entry.content.trim())
            append("\n")
        }
    }

    /**
     * 解析导入的 JSON 字符串（容错解析标准对象或直接数组）
     */
    fun parseJson(rawInput: String): BackupParseResult {
        var cleanInput = rawInput.trim()
        if (cleanInput.isEmpty()) {
            return BackupParseResult(
                success = false,
                errorMessage = "输入数据为空，请粘贴或选择有效的 JSON 备份数据。"
            )
        }

        // 移除可能存在的 UTF-8 BOM
        if (cleanInput.startsWith("\uFEFF")) {
            cleanInput = cleanInput.substring(1).trim()
        }

        try {
            val entriesArray: JSONArray
            var appName = "AI智能日记"
            var version = 1
            var exportedAt = System.currentTimeMillis()
            var exportedAtFormatted = ""

            if (cleanInput.startsWith("{")) {
                val root = JSONObject(cleanInput)
                version = root.optInt("version", 1)
                appName = root.optString("appName", "AI智能日记")
                exportedAt = root.optLong("exportedAt", System.currentTimeMillis())
                exportedAtFormatted = root.optString(
                    "exportedAtFormatted",
                    sdfDateTime.format(Date(exportedAt))
                )

                if (root.has("entries")) {
                    entriesArray = root.getJSONArray("entries")
                } else {
                    return BackupParseResult(
                        success = false,
                        errorMessage = "JSON 缺少 entries 数组字段，无法读取日记条目。"
                    )
                }
            } else if (cleanInput.startsWith("[")) {
                // 直接是条目数组
                entriesArray = JSONArray(cleanInput)
                exportedAtFormatted = sdfDateTime.format(Date(exportedAt))
            } else {
                return BackupParseResult(
                    success = false,
                    errorMessage = "数据格式不符合 JSON 规范，必须以 '{' 或 '[' 开头。"
                )
            }

            if (entriesArray.length() == 0) {
                return BackupParseResult(
                    success = false,
                    errorMessage = "备份数据中的日记条目列表为空，未找到任何记录。"
                )
            }

            val parsedList = mutableListOf<DiaryEntry>()
            for (i in 0 until entriesArray.length()) {
                val obj = entriesArray.optJSONObject(i) ?: continue

                val content = obj.optString("content", "")
                val title = obj.optString("title", "")
                if (content.isBlank() && title.isBlank()) {
                    continue // 跳过空白脏数据
                }

                val mood = obj.optString("mood", "😊")
                val moodLabel = obj.optString("moodLabel", "开心")
                val weather = obj.optString("weather", "☀️")
                val weatherLabel = obj.optString("weatherLabel", "晴")
                val temperature = obj.optString("temperature", "")
                val location = obj.optString("location", "")
                val category = obj.optString("category", "日常").ifBlank { "日常" }
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val updatedAt = obj.optLong("updatedAt", createdAt)
                val isFavorite = obj.optBoolean("isFavorite", false)

                // 图片解析容错
                var imagesJson = obj.optString("imagesJson", "")
                if (imagesJson.isBlank() && obj.has("images")) {
                    val imgArr = obj.optJSONArray("images")
                    if (imgArr != null && imgArr.length() > 0) {
                        val imgList = mutableListOf<String>()
                        for (k in 0 until imgArr.length()) {
                            imgList.add(imgArr.optString(k))
                        }
                        imagesJson = DiaryEntry.formatImages(imgList)
                    }
                }

                parsedList.add(
                    DiaryEntry(
                        id = 0L, // 导入时置 0，由 Room 或合并策略分配新 ID，防止冲突
                        title = title,
                        content = content,
                        mood = mood,
                        moodLabel = moodLabel,
                        weather = weather,
                        weatherLabel = weatherLabel,
                        temperature = temperature,
                        location = location,
                        imagesJson = imagesJson,
                        category = category,
                        createdAt = createdAt,
                        updatedAt = updatedAt,
                        isFavorite = isFavorite,
                        wordCount = DiaryEntry.calculateWordCount(content)
                    )
                )
            }

            if (parsedList.isEmpty()) {
                return BackupParseResult(
                    success = false,
                    errorMessage = "未能解析出任何有效的日记内容，请检查文件格式。"
                )
            }

            val sortedByTime = parsedList.sortedBy { it.createdAt }
            val earliestDate = sdfDate.format(Date(sortedByTime.first().createdAt))
            val latestDate = sdfDate.format(Date(sortedByTime.last().createdAt))
            val totalWords = parsedList.sumOf { it.wordCount }

            val metadata = BackupMetadata(
                version = version,
                appName = appName,
                exportedAt = exportedAt,
                exportedAtFormatted = exportedAtFormatted,
                entryCount = parsedList.size,
                totalWords = totalWords,
                earliestDate = earliestDate,
                latestDate = latestDate
            )

            return BackupParseResult(
                success = true,
                metadata = metadata,
                entries = parsedList
            )
        } catch (e: Exception) {
            return BackupParseResult(
                success = false,
                errorMessage = "JSON 格式解析异常: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    /**
     * 体验用示例备份 JSON 数据，便于用户无文件时一键体验导入功能
     */
    fun getSampleBackupJson(): String {
        val now = System.currentTimeMillis()
        val oneDayAgo = now - 24 * 60 * 60 * 1000L
        val sampleEntries = listOf(
            DiaryEntry(
                title = "初秋微风与一杯手冲 ☕",
                content = "# 惬意的小憩\n\n下午在街角的咖啡馆坐了一会儿，点了一杯耶加雪菲。窗外偶尔有金黄色的落叶飘过，阳光透过玻璃照在木质桌面上，暖洋洋的。\n\n> 偷得浮生半日闲，生活其实很美好。\n\n- 完成了本周的阅读计划第 3 章\n- 听了一张古典爵士黑胶唱片\n- 享受完全属于自己的独处时光",
                mood = "😌",
                moodLabel = "平静",
                weather = "🌤️",
                weatherLabel = "晴间多云",
                temperature = "23°C",
                location = "街角咖啡馆",
                category = "日常",
                createdAt = oneDayAgo,
                updatedAt = oneDayAgo,
                isFavorite = true
            ),
            DiaryEntry(
                title = "周末山野漫步与观星 ✨",
                content = "# 远离喧嚣的治愈之旅 🌲\n\n和朋友一起爬上山顶，呼吸着松针与泥土的清香。夜晚山上的星星格外明亮，银河如同一条轻柔的白练横跨天幕，整颗心都变得辽阔而安宁。\n\n每一次走向大自然，都是给心灵充电的过程。",
                mood = "✨",
                moodLabel = "兴奋",
                weather = "🌙",
                weatherLabel = "晴夜",
                temperature = "18°C",
                location = "莫干山顶",
                category = "旅行",
                createdAt = now,
                updatedAt = now,
                isFavorite = false
            )
        )
        return exportToJson(sampleEntries)
    }
}
