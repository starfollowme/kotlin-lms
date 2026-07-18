package com.mext.utbk.tutor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mext.utbk.tutor.data.local.dao.*
import com.mext.utbk.tutor.data.local.entity.*

@Database(
    entities = [
        ChatMessageEntity::class,
        BookmarkEntity::class,
        StudyPlanEntity::class,
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tutor_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
