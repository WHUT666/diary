package com.example.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DiaryExportManager {

    /**
     * Formats selected entries into an elegant Markdown diary book.
     */
    fun generateMarkdownBook(entries: List<DiaryEntry>, bookTitle: String = "我的生活日记全集"): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE)
        val exportTimeStr = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINESE).format(Date())

        val sb = StringBuilder()
        sb.appendLine("# 📖 $bookTitle")
        sb.appendLine("> 导出时间：$exportTimeStr")
        sb.appendLine("> 收录篇数：${entries.size} 篇")
        sb.appendLine("> 累计字数：${entries.sumOf { it.wordCount }} 字")
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()

        val sortedEntries = entries.sortedBy { it.createdAt }

        sortedEntries.forEachIndexed { index, entry ->
            val dateStr = sdf.format(Date(entry.createdAt))
            val title = if (entry.title.isNotBlank()) entry.title else "日记 #${index + 1}"
            sb.appendLine("## $title")
            sb.appendLine()
            sb.appendLine("- **时间：** $dateStr")
            sb.appendLine("- **心境：** ${entry.mood} ${entry.moodLabel}")
            sb.appendLine("- **天气：** ${entry.weather} ${entry.weatherLabel} ${entry.temperature}")
            if (entry.location.isNotBlank()) {
                sb.appendLine("- **地点：** 📍 ${entry.location}")
            }
            if (entry.category.isNotBlank()) {
                sb.appendLine("- **分类：** 🏷️ ${entry.category}")
            }
            if (entry.audioPath.isNotBlank()) {
                sb.appendLine("- **语音原声：** 🎙️ 录音时长 ${entry.audioDurationSec} 秒")
            }
            if (entry.isFavorite) {
                sb.appendLine("- **标记：** ⭐ 特别珍藏")
            }
            sb.appendLine()
            sb.appendLine(entry.content)
            sb.appendLine()
            sb.appendLine("---")
            sb.appendLine()
        }

        sb.appendLine("*本电子书由 极简智能日记 本地安全导出*")
        return sb.toString()
    }

    /**
     * Formats entries into a clean Plain Text diary book.
     */
    fun generatePlainTextBook(entries: List<DiaryEntry>, bookTitle: String = "我的生活日记全集"): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE)
        val exportTimeStr = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINESE).format(Date())

        val sb = StringBuilder()
        sb.appendLine("========================================")
        sb.appendLine("        $bookTitle")
        sb.appendLine("  导出时间：$exportTimeStr")
        sb.appendLine("  共收录 ${entries.size} 篇日记，总计 ${entries.sumOf { it.wordCount }} 字")
        sb.appendLine("========================================")
        sb.appendLine()

        val sortedEntries = entries.sortedBy { it.createdAt }

        sortedEntries.forEachIndexed { index, entry ->
            val dateStr = sdf.format(Date(entry.createdAt))
            val title = if (entry.title.isNotBlank()) entry.title else "第 ${index + 1} 篇"
            sb.appendLine("【$title】")
            sb.appendLine("时间：$dateStr")
            sb.appendLine("心境：${entry.mood} ${entry.moodLabel}  |  天气：${entry.weather} ${entry.weatherLabel} ${entry.temperature}")
            if (entry.location.isNotBlank()) {
                sb.appendLine("地点：${entry.location}")
            }
            sb.appendLine("----------------------------------------")
            sb.appendLine(entry.content)
            sb.appendLine()
            sb.appendLine()
        }

        return sb.toString()
    }

    /**
     * Writes content to a file and launches the Android Share Intent.
     */
    fun shareExportedFile(context: Context, content: String, fileName: String, mimeType: String = "text/markdown"): Boolean {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)
            FileOutputStream(file).use {
                it.write(content.toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "导出与分享日记电子书")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
