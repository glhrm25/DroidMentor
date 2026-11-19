package com.example.droidmentor.data.repository

import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the persistence of conversations (chat sessions) and their messages.
 *
 * Implementations:
 * - [RoomConversationRepository]: production implementation backed by Room.
 * - [FakeConversationRepository]: in-memory implementation for tests and previews.
 */
interface ConversationRepository {

    /**
     * Observable stream of all conversations ordered by recent activity.
     */
    val allConversations: Flow<List<ConversationEntity>>

    /**
     * Retrieves a single conversation by its ID.
     */
    suspend fun getConversationById(id: Long): ConversationEntity?

    /**
     * Retrieves the currently active conversation, if any.
     */
    suspend fun getActiveConversation(): ConversationEntity?

    /**
     * Creates a new conversation with the specified title and returns its ID.
     */
    suspend fun createConversation(title: String): Long

    /**
     * Updates an existing conversation.
     */
    suspend fun updateConversation(conversation: ConversationEntity)

    /**
     * Deletes a conversation by its ID.
     */
    suspend fun deleteConversation(id: Long)

    /**
     * Sets a conversation as the active session.
     */
    suspend fun setActiveConversation(id: Long)

    /**
     * Clears any active conversation flag.
     */
    suspend fun clearActiveConversation()

    /**
     * Observes messages belonging to a conversation, in chronological order.
     */
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>>

    /**
     * Inserts a message into the conversation and updates the conversation's updatedAt timestamp.
     */
    suspend fun addMessage(conversationId: Long, role: String, text: String): Long

    /**
     * Deletes all messages in a conversation sent after a given timestamp (for editing/rewriting).
     */
    suspend fun deleteMessagesAfter(conversationId: Long, timestamp: Long)
}
