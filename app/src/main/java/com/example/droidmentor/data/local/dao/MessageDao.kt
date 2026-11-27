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
     * Messages with the same timestamp are kept in insertion order (by identifier).
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC, id ASC")
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>

    /**
     * Retrieves all messages for a conversation as a one-shot query, in the same order as
     * [getMessagesForConversation].
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC, id ASC")
    suspend fun getMessagesForConversationSync(conversationId: Long): List<MessageEntity>

    /**
     * Retrieves a single message by its identifier.
     */
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): MessageEntity?

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
     * Deletes the message with the given identifier.
     * @return The number of deleted messages (0 or 1).
     */
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: Long): Int

    /**
     * Deletes the message with the given identifier and every message that follows it in the
     * same conversation, in a single statement (hence atomically). The order is the one of
     * [getMessagesForConversation]: by timestamp and, for equal timestamps, by identifier.
     * Used when a message is rewritten, as the messages that follow it are no longer valid.
     * @return The number of deleted messages (0 if there is no message with the given identifier).
     */
    @Query(
        """
        DELETE FROM messages
        WHERE conversationId = (SELECT conversationId FROM messages WHERE id = :messageId)
          AND (timestamp > (SELECT timestamp FROM messages WHERE id = :messageId)
               OR (timestamp = (SELECT timestamp FROM messages WHERE id = :messageId)
                   AND id >= :messageId))
        """
    )
    suspend fun deleteMessagesFrom(messageId: Long): Int

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
