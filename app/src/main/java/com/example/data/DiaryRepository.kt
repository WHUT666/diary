package com.example.data

import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {

    val allEntries: Flow<List<DiaryEntry>> = diaryDao.getAllEntries()

    fun searchEntries(query: String): Flow<List<DiaryEntry>> {
        return if (query.isBlank()) {
            diaryDao.getAllEntries()
        } else {
            diaryDao.searchEntries(query.trim())
        }
    }

    suspend fun getEntryById(id: Long): DiaryEntry? {
        return diaryDao.getEntryById(id)
    }

    suspend fun insertOrUpdate(entry: DiaryEntry): Long {
        return if (entry.id == 0L) {
            diaryDao.insertEntry(
                entry.copy(
                    updatedAt = System.currentTimeMillis(),
                    wordCount = DiaryEntry.calculateWordCount(entry.content)
                )
            )
        } else {
            diaryDao.updateEntry(
                entry.copy(
                    updatedAt = System.currentTimeMillis(),
                    wordCount = DiaryEntry.calculateWordCount(entry.content)
                )
            )
            entry.id
        }
    }

    suspend fun delete(entry: DiaryEntry) {
        diaryDao.deleteEntry(entry)
    }

    suspend fun deleteById(id: Long) {
        diaryDao.deleteEntryById(id)
    }

    suspend fun toggleFavorite(entry: DiaryEntry) {
        diaryDao.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
    }

    suspend fun getAllEntriesSnapshot(): List<DiaryEntry> {
        return diaryDao.getAllEntriesSnapshot()
    }

    suspend fun deleteAllEntries() {
        diaryDao.deleteAllEntries()
    }

    suspend fun importEntries(
        newEntries: List<DiaryEntry>,
        strategy: ImportStrategy
    ): ImportReport {
        if (newEntries.isEmpty()) {
            val total = diaryDao.getAllEntriesSnapshot().size
            return ImportReport(0, 0, 0, total)
        }

        return when (strategy) {
            ImportStrategy.OVERWRITE_ALL -> {
                diaryDao.deleteAllEntries()
                val prepared = newEntries.map {
                    it.copy(
                        id = 0L,
                        wordCount = DiaryEntry.calculateWordCount(it.content)
                    )
                }
                diaryDao.insertAll(prepared)
                ImportReport(
                    totalInBackup = newEntries.size,
                    newlyInserted = newEntries.size,
                    skippedDuplicates = 0,
                    totalNow = newEntries.size
                )
            }
            ImportStrategy.APPEND_ALL -> {
                val prepared = newEntries.map {
                    it.copy(
                        id = 0L,
                        wordCount = DiaryEntry.calculateWordCount(it.content)
                    )
                }
                diaryDao.insertAll(prepared)
                val total = diaryDao.getAllEntriesSnapshot().size
                ImportReport(
                    totalInBackup = newEntries.size,
                    newlyInserted = newEntries.size,
                    skippedDuplicates = 0,
                    totalNow = total
                )
            }
            ImportStrategy.SMART_MERGE -> {
                val existing = diaryDao.getAllEntriesSnapshot()
                val existingSignatures = existing.map {
                    "${it.createdAt / 2000}_${it.title.trim()}_${it.content.trim()}"
                }.toSet()

                val toInsert = mutableListOf<DiaryEntry>()
                var skipped = 0

                for (entry in newEntries) {
                    val sig = "${entry.createdAt / 2000}_${entry.title.trim()}_${entry.content.trim()}"
                    if (existingSignatures.contains(sig)) {
                        skipped++
                    } else {
                        toInsert.add(
                            entry.copy(
                                id = 0L,
                                wordCount = DiaryEntry.calculateWordCount(entry.content)
                            )
                        )
                    }
                }

                if (toInsert.isNotEmpty()) {
                    diaryDao.insertAll(toInsert)
                }

                val total = diaryDao.getAllEntriesSnapshot().size
                ImportReport(
                    totalInBackup = newEntries.size,
                    newlyInserted = toInsert.size,
                    skippedDuplicates = skipped,
                    totalNow = total
                )
            }
        }
    }
}
