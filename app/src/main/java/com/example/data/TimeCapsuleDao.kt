package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeCapsuleDao {

    @Query("SELECT * FROM time_capsules ORDER BY createdAt DESC")
    fun getAllCapsules(): Flow<List<TimeCapsule>>

    @Query("SELECT * FROM time_capsules WHERE id = :id LIMIT 1")
    suspend fun getCapsuleById(id: Long): TimeCapsule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCapsule(capsule: TimeCapsule): Long

    @Update
    suspend fun updateCapsule(capsule: TimeCapsule)

    @Delete
    suspend fun deleteCapsule(capsule: TimeCapsule)

    @Query("UPDATE time_capsules SET isUnlocked = 1, reflectionResponse = :response, responseTimestamp = :responseTime WHERE id = :id")
    suspend fun recordReflection(id: Long, response: String, responseTime: Long)

    @Query("UPDATE time_capsules SET isUnlocked = 1 WHERE id = :id")
    suspend fun markUnlocked(id: Long)
}
