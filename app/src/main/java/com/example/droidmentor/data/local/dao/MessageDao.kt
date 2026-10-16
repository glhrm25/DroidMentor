package com.example.droidmentor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.droidmentor.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing [MessageEntity] records in the Room database.
 */
@Dao
interface MessageDao {

    /**
     * Observes all messages associated with a conversation, ordered chronologically.
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    /**
     * Retrieves all messages for a conversation as a one-shot query.
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversationSync(conversationId: Long): List<MessageEntity>

    /**
     * Inserts a single message into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    /**
     * Inserts multiple messages into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>): List<Long>

    /**
     * Updates an existing message.
     */
    @Update
    suspend fun updateMessage(message: MessageEntity): Int

    /**
     * Deletes a specific message.
     */
    @Delete
    suspend fun deleteMessage(message: MessageEntity): Int

    /**
     * Deletes all messages belonging to a conversation.
     */
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long): Int

    /**
     * Deletes all messages in a conversation recorded after a given timestamp.
     * Useful for conversation rewriting / branching as required by the assignment.
     */
    @Query("DELETE FROM messages WHERE conversationId = :conversationId AND timestamp > :timestamp")
    suspend fun deleteMessagesAfter(conversationId: Long, timestamp: Long): Int
}
