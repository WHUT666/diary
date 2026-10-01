package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TodoPriority(val label: String, val colorHex: Long) {
    HIGH("重要", 0xFFE76F51),
    NORMAL("普通", 0xFFE9C46A),
    LOW("低优先", 0xFF2A9D8F)
}

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = TodoPriority.NORMAL.name,
    val dueDate: Long? = null,
    val relatedDiaryId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val priorityEnum: TodoPriority
        get() = try {
            TodoPriority.valueOf(priority)
        } catch (e: Exception) {
            TodoPriority.NORMAL
        }

    val formattedDueDate: String?
        get() = dueDate?.let {
            val sdf = SimpleDateFormat("MM月dd日", Locale.CHINESE)
            sdf.format(Date(it))
        }

    val formattedCreatedDate: String
        get() {
            val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.CHINESE)
            return sdf.format(Date(createdAt))
        }
}
