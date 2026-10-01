package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DiaryEntry::class, TodoItem::class, TimeCapsule::class],
    version = 4,
    exportSchema = false
)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun todoDao(): TodoDao
    abstract fun timeCapsuleDao(): TimeCapsuleDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN audioPath TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN audioDurationSec INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `time_capsules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `sealTag` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `unlockAt` INTEGER NOT NULL,
                        `isUnlocked` INTEGER NOT NULL,
                        `reflectionResponse` TEXT NOT NULL,
                        `responseTimestamp` INTEGER,
                        `mood` TEXT NOT NULL,
                        `themeCover` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                )
                    .addCallback(DiaryDatabaseCallback())
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DiaryDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.diaryDao(), database.todoDao(), database.timeCapsuleDao())
                    }
                }
            }

            private suspend fun populateInitialData(
                diaryDao: DiaryDao,
                todoDao: TodoDao,
                timeCapsuleDao: TimeCapsuleDao
            ) {
                val now = System.currentTimeMillis()
                val oneDayMillis = 24 * 60 * 60 * 1000L

                val entry1 = DiaryEntry(
                    title = "开启我的第一篇日记",
                    content = "# 生活的微光 ✨\n\n> 生活是由每一个平凡而又微小的瞬间组成的。\n\n今天开始用这本小小的日记，记录下呼吸时的宁静、阳光穿过树叶的光斑、还有那些不经意间涌上心头的感动与思考。\n\n- 晨间在阳台深呼吸\n- 喝到一杯温度适宜的温水\n- 翻开一本喜欢的书\n\n希望未来的自己翻看时，能感受到当下的真诚与温度。",
                    mood = "✨",
                    moodLabel = "兴奋",
                    weather = "☀️",
                    weatherLabel = "晴朗",
                    temperature = "25°C",
                    location = "🏠 家中阳台",
                    category = "随想",
                    createdAt = now,
                    updatedAt = now,
                    isFavorite = true,
                    wordCount = 115
                )

                val entry2 = DiaryEntry(
                    title = "初秋微风与书香",
                    content = "下午去街角的小咖啡馆坐了一会儿，窗外风吹得树叶沙沙作响。\n\n> 偶尔从快节奏的生活中抽身出来，给心灵留白，真的是一种不可多得的治愈。\n\n点了一杯热拿铁，读完了那本搁置已久的书。秋意渐浓，愿我们都能在岁月中找到内心的安宁。",
                    mood = "😌",
                    moodLabel = "平静",
                    weather = "⛅",
                    weatherLabel = "多云",
                    temperature = "21°C",
                    location = "☕ 街角咖啡馆",
                    category = "感悟",
                    createdAt = now - oneDayMillis,
                    updatedAt = now - oneDayMillis,
                    isFavorite = false,
                    wordCount = 96
                )

                val diaryId1 = diaryDao.insertEntry(entry1)
                diaryDao.insertEntry(entry2)

                // Populate helpful initial todos
                val todo1 = TodoItem(
                    title = "写下今天的一件微小确幸",
                    isCompleted = false,
                    priority = TodoPriority.HIGH.name,
                    relatedDiaryId = diaryId1,
                    createdAt = now
                )
                val todo2 = TodoItem(
                    title = "用 AI 灵感驿站获取今日名言",
                    isCompleted = true,
                    priority = TodoPriority.NORMAL.name,
                    createdAt = now - 3600000L,
                    completedAt = now - 1800000L
                )
                val todo3 = TodoItem(
                    title = "尝试用 AI 工具智能拆解复杂目标",
                    isCompleted = false,
                    priority = TodoPriority.NORMAL.name,
                    createdAt = now
                )

                todoDao.insertTodo(todo1)
                todoDao.insertTodo(todo2)
                todoDao.insertTodo(todo3)

                // Populate initial time capsules
                val capsule1 = TimeCapsule(
                    title = "给一年后的自己：保持好奇与热爱",
                    content = "你好，一年后的我！当你开启这封信时，不知道我们是否已经实现了心中的那个梦想？希望你依然保留着对世界的好奇心，在忙碌的工作之余别忘了在傍晚抬头看看晚霞。永远记得：慢一点也没关系，步履不停就好。",
                    sealTag = "致一年后的自己",
                    createdAt = now - 7 * oneDayMillis,
                    unlockAt = now + 90 * oneDayMillis,
                    isUnlocked = false,
                    mood = "🌟",
                    themeCover = "GALAXY"
                )

                val capsule2 = TimeCapsule(
                    title = "百日之约：写在秋天来临前",
                    content = "初秋微凉，给自己许下三个愿望：读完十本书、坚持每周运动、用心感受身边每一个具体的瞬间。",
                    sealTag = "百日心愿",
                    createdAt = now - 100 * oneDayMillis,
                    unlockAt = now - oneDayMillis,
                    isUnlocked = true,
                    reflectionResponse = "回头看当时的愿望，虽然有些还在推进中，但生活确实更加丰富和安宁了。感谢当初真诚许愿的自己！",
                    responseTimestamp = now,
                    mood = "🍁",
                    themeCover = "SANDGLASS"
                )

                timeCapsuleDao.insertCapsule(capsule1)
                timeCapsuleDao.insertCapsule(capsule2)
            }
        }
    }
}
