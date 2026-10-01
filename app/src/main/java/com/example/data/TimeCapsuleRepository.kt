package com.example.data

import kotlinx.coroutines.flow.Flow

class TimeCapsuleRepository(private val timeCapsuleDao: TimeCapsuleDao) {

    val allCapsules: Flow<List<TimeCapsule>> = timeCapsuleDao.getAllCapsules()

    suspend fun getCapsuleById(id: Long): TimeCapsule? {
        return timeCapsuleDao.getCapsuleById(id)
    }

    suspend fun insertCapsule(capsule: TimeCapsule): Long {
        return timeCapsuleDao.insertCapsule(capsule)
    }

    suspend fun updateCapsule(capsule: TimeCapsule) {
        timeCapsuleDao.updateCapsule(capsule)
    }

    suspend fun deleteCapsule(capsule: TimeCapsule) {
        timeCapsuleDao.deleteCapsule(capsule)
    }

    suspend fun recordReflection(id: Long, response: String) {
        timeCapsuleDao.recordReflection(id, response, System.currentTimeMillis())
    }

    suspend fun markUnlocked(id: Long) {
        timeCapsuleDao.markUnlocked(id)
    }
}
