package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiaryDraft(
    val draftKey: String,
    val targetEntryId: Long = 0L,
    val title: String = "",
    val content: String = "",
    val mood: String = "✨",
    val moodLabel: String = "开心",
    val weather: String = "☀️",
    val weatherLabel: String = "晴朗",
    val temperature: String = "24℃",
    val location: String = "",
    val images: List<String> = emptyList(),
    val category: String = "日常",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val lastSavedAt: Long = System.currentTimeMillis()
) {
    fun isNotEmpty(): Boolean {
        return title.isNotBlank() || content.isNotBlank() || images.isNotEmpty()
    }

    val wordCount: Int
        get() = DiaryEntry.calculateWordCount(content)

    val formattedSavedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(lastSavedAt))
        }

    val formattedFullSavedTime: String
        get() {
            val sdf = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
            return sdf.format(Date(lastSavedAt))
        }

    fun toJson(): String {
        val root = JSONObject()
        root.put("draftKey", draftKey)
        root.put("targetEntryId", targetEntryId)
        root.put("title", title)
        root.put("content", content)
        root.put("mood", mood)
        root.put("moodLabel", moodLabel)
        root.put("weather", weather)
        root.put("weatherLabel", weatherLabel)
        root.put("temperature", temperature)
        root.put("location", location)

        val imgArray = JSONArray()
        images.forEach { imgArray.put(it) }
        root.put("images", imgArray)

        root.put("category", category)
        root.put("timestamp", timestamp)
        root.put("isFavorite", isFavorite)
        root.put("lastSavedAt", lastSavedAt)
        return root.toString()
    }

    companion object {
        fun fromJson(rawJson: String): DiaryDraft? {
            return try {
                val obj = JSONObject(rawJson)
                val imagesList = mutableListOf<String>()
                val imgArray = obj.optJSONArray("images")
                if (imgArray != null) {
                    for (i in 0 until imgArray.length()) {
                        imagesList.add(imgArray.getString(i))
                    }
                }

                DiaryDraft(
                    draftKey = obj.optString("draftKey", "draft_new"),
                    targetEntryId = obj.optLong("targetEntryId", 0L),
                    title = obj.optString("title", ""),
                    content = obj.optString("content", ""),
                    mood = obj.optString("mood", "✨"),
                    moodLabel = obj.optString("moodLabel", "开心"),
                    weather = obj.optString("weather", "☀️"),
                    weatherLabel = obj.optString("weatherLabel", "晴朗"),
                    temperature = obj.optString("temperature", "24℃"),
                    location = obj.optString("location", ""),
                    images = imagesList,
                    category = obj.optString("category", "日常"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    lastSavedAt = obj.optLong("lastSavedAt", System.currentTimeMillis())
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

class DiaryDraftManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("diary_drafts_v1", Context.MODE_PRIVATE)

    companion object {
        const val KEY_NEW_ENTRY = "draft_new"

        fun keyForEntry(entryId: Long?): String {
            return if (entryId == null || entryId <= 0L) {
                KEY_NEW_ENTRY
            } else {
                "draft_edit_$entryId"
            }
        }
    }

    fun saveDraft(draft: DiaryDraft) {
        if (!draft.isNotEmpty()) {
            clearDraft(draft.draftKey)
            return
        }
        val json = draft.toJson()
        prefs.edit().putString(draft.draftKey, json).apply()
    }

    fun getDraft(draftKey: String): DiaryDraft? {
        val raw = prefs.getString(draftKey, null) ?: return null
        return DiaryDraft.fromJson(raw)
    }

    fun hasDraft(draftKey: String): Boolean {
        val draft = getDraft(draftKey)
        return draft != null && draft.isNotEmpty()
    }

    fun clearDraft(draftKey: String) {
        prefs.edit().remove(draftKey).apply()
    }

    fun getNewEntryDraft(): DiaryDraft? {
        return getDraft(KEY_NEW_ENTRY)
    }

    fun clearNewEntryDraft() {
        clearDraft(KEY_NEW_ENTRY)
    }
}
