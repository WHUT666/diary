package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): DiaryEntry?

    @Query("SELECT * FROM diary_entries WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchEntries(query: String): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE moodLabel = :moodLabel ORDER BY createdAt DESC")
    fun getEntriesByMood(moodLabel: String): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE category = :category ORDER BY createdAt DESC")
    fun getEntriesByCategory(category: String): Flow<List<DiaryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DiaryEntry>): List<Long>

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    suspend fun getAllEntriesSnapshot(): List<DiaryEntry>

    @Update
    suspend fun updateEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM diary_entries")
    suspend fun deleteAllEntries()

    @Query("SELECT COUNT(*) FROM diary_entries")
    fun getEntryCount(): Flow<Int>
}
