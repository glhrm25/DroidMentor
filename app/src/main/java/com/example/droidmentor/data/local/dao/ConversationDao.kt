package com.example.droidmentor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.droidmentor.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing [ConversationEntity] records in the Room database.
 */
@Dao
interface ConversationDao {

    /**
     * Retrieves all conversations ordered by the most recently updated first.
     */
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    /**
     * Retrieves a single conversation by its ID.
     */
    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): ConversationEntity?

    /**
     * Retrieves the currently active conversation, if one exists.
     */
    @Query("SELECT * FROM conversations WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveConversation(): ConversationEntity?

    /**
     * Inserts a new conversation and returns its generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    /**
     * Updates an existing conversation.
     */
    @Update
    suspend fun updateConversation(conversation: ConversationEntity): Int

    /**
     * Deletes a conversation from the database. Cascading delete removes associated messages.
     */
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity): Int

    /**
     * Deletes a conversation by its ID.
     */
    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long): Int

    /**
     * Marks all conversations as inactive.
     */
    @Query("UPDATE conversations SET isActive = 0")
    suspend fun clearActiveConversation(): Int

    /**
     * Marks a specific conversation as active.
     */
    @Query("UPDATE conversations SET isActive = 1 WHERE id = :id")
    suspend fun setActiveConversation(id: Long): Int
}
