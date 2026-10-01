package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "time_capsules")
data class TimeCapsule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val sealTag: String = "给未来的自己",
    val createdAt: Long = System.currentTimeMillis(),
    val unlockAt: Long,
    val isUnlocked: Boolean = false,
    val reflectionResponse: String = "",
    val responseTimestamp: Long? = null,
    val mood: String = "🌟",
    val themeCover: String = "GALAXY" // GALAXY, SANDGLASS, SAKURA, VINTAGE
) {
    val isReadyToUnlock: Boolean
        get() = isUnlocked || System.currentTimeMillis() >= unlockAt

    val remainingDays: Long
        get() {
            val diff = unlockAt - System.currentTimeMillis()
            return if (diff > 0) diff / (24 * 60 * 60 * 1000L) else 0L
        }

    val remainingHours: Long
        get() {
            val diff = unlockAt - System.currentTimeMillis()
            return if (diff > 0) diff / (60 * 60 * 1000L) else 0L
        }

    val progress: Float
        get() {
            val total = (unlockAt - createdAt).coerceAtLeast(1L)
            val elapsed = (System.currentTimeMillis() - createdAt).coerceAtLeast(0L)
            return (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }

    val formattedCreatedDate: String
        get() = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE).format(Date(createdAt))

    val formattedUnlockDate: String
        get() = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINESE).format(Date(unlockAt))
}
