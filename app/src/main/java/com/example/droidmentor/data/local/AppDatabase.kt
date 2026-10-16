package com.example.droidmentor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.droidmentor.data.local.dao.ConversationDao
import com.example.droidmentor.data.local.dao.MessageDao
import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity

/**
 * Main Room database for the DroidMentor application.
 * Manages persistent storage of conversations and message histories.
 */
@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "droidmentor_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
