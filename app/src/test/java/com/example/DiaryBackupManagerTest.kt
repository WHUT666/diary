package com.example

import com.example.data.DiaryBackupManager
import com.example.data.DiaryEntry
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryBackupManagerTest {

    private val sampleEntries = listOf(
        DiaryEntry(
            id = 1L,
            title = "秋日的第一杯咖啡",
            content = "今天早晨漫步在桂花树下，风吹过带着阵阵清香。在转角的咖啡馆点了一杯桂花拿铁。",
            mood = "☕",
            moodLabel = "悠闲",
            weather = "🌤️",
            weatherLabel = "多云",
            temperature = "22℃",
            location = "西湖湖畔",
            category = "生活感悟",
            isFavorite = true,
            createdAt = 1726470000000L,
            updatedAt = 1726470000000L,
            wordCount = 38,
            imagesJson = "https://images.unsplash.com/photo-1|||https://images.unsplash.com/photo-2"
        ),
        DiaryEntry(
            id = 2L,
            title = "技术思考与沉淀",
            content = "完成了一个日记应用离线备份和恢复功能的设计，支持 JSON 与 Markdown 双格式。",
            mood = "💡",
            moodLabel = "灵感",
            weather = "☀️",
            weatherLabel = "晴朗",
            temperature = "25℃",
            location = "书房",
            category = "灵感随想",
            isFavorite = false,
            createdAt = 1726556400000L,
            updatedAt = 1726556400000L,
            wordCount = 42,
            imagesJson = ""
        )
    )

    @Test
    fun exportToJson_createsValidJsonWithMetadata() {
        val jsonString = DiaryBackupManager.exportToJson(sampleEntries)
        assertNotNull(jsonString)
        assertTrue(jsonString.isNotBlank())

        val root = JSONObject(jsonString)
        assertEquals(1, root.getInt("version"))
        assertEquals("AI智能日记", root.getString("appName"))
        assertEquals(2, root.getInt("entryCount"))

        val entriesArray = root.getJSONArray("entries")
        assertEquals(2, entriesArray.length())

        val first = entriesArray.getJSONObject(0)
        assertEquals("秋日的第一杯咖啡", first.getString("title"))
        assertEquals("☕", first.getString("mood"))
        assertEquals(true, first.getBoolean("isFavorite"))
        val images = first.getJSONArray("images")
        assertEquals(2, images.length())
    }

    @Test
    fun parseJson_successfullyParsesValidExportedJson() {
        val exportedJson = DiaryBackupManager.exportToJson(sampleEntries)
        val result = DiaryBackupManager.parseJson(exportedJson)

        assertTrue(result.success)
        assertNotNull(result.metadata)
        assertEquals(2, result.entries.size)

        val first = result.entries[0]
        assertEquals("秋日的第一杯咖啡", first.title)
        assertEquals("悠闲", first.moodLabel)
        assertEquals("西湖湖畔", first.location)
        assertTrue(first.isFavorite)
        assertTrue(first.imagesJson.contains("photo-1"))
        assertTrue(first.imagesJson.contains("photo-2"))
    }

    @Test
    fun parseJson_handlesInvalidJsonGracefully() {
        val result = DiaryBackupManager.parseJson("invalid non-json text string")
        assertFalse(result.success)
        assertNotNull(result.errorMessage)
        assertTrue(result.entries.isEmpty())
    }

    @Test
    fun getSampleBackupJson_parsesSuccessfully() {
        val sample = DiaryBackupManager.getSampleBackupJson()
        val result = DiaryBackupManager.parseJson(sample)

        assertTrue(result.success)
        assertTrue(result.entries.isNotEmpty())
    }

    @Test
    fun exportToMarkdown_createsFormattedDocument() {
        val markdown = DiaryBackupManager.exportToMarkdown(sampleEntries)

        assertTrue(markdown.contains("我的日记本 · 完整归档备份"))
        assertTrue(markdown.contains("秋日的第一杯咖啡"))
        assertTrue(markdown.contains("技术思考与沉淀"))
        assertTrue(markdown.contains("西湖湖畔"))
        assertTrue(markdown.contains("生活感悟"))
    }
}
