package com.example.droidmentor.data.repository

import com.example.droidmentor.data.local.dao.ConversationDao
import com.example.droidmentor.data.local.dao.MessageDao
import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository providing an abstraction layer over local Room persistence
 * for conversations and messages.
 *
 * @property conversationDao DAO for conversation operations.
 * @property messageDao DAO for message operations.
 */
class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {

    /**
     * Observable stream of all conversations ordered by recent activity.
     */
    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    /**
     * Retrieves a single conversation by its ID.
     */
    suspend fun getConversationById(id: Long): ConversationEntity? {
        return conversationDao.getConversationById(id)
    }

    /**
     * Retrieves the currently active conversation, if any.
     */
    suspend fun getActiveConversation(): ConversationEntity? {
        return conversationDao.getActiveConversation()
    }

    /**
     * Creates a new conversation with the specified title and returns its ID.
     */
    suspend fun createConversation(title: String): Long {
        val now = System.currentTimeMillis()
        val conversation = ConversationEntity(
            title = title,
            createdAt = now,
            updatedAt = now,
            isActive = true
        )
        return conversationDao.insertConversation(conversation)
    }

    /**
     * Updates an existing conversation.
     */
    suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }

    /**
     * Deletes a conversation by its ID.
     */
    suspend fun deleteConversation(id: Long) {
        conversationDao.deleteConversationById(id)
    }

    /**
     * Sets a conversation as the active session.
     */
    suspend fun setActiveConversation(id: Long) {
        conversationDao.clearActiveConversation()
        conversationDao.setActiveConversation(id)
    }

    /**
     * Clears any active conversation flag.
     */
    suspend fun clearActiveConversation() {
        conversationDao.clearActiveConversation()
    }

    /**
     * Observes messages belonging to a conversation.
     */
    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    /**
     * Inserts a message into the conversation and updates the conversation's updatedAt timestamp.
     */
    suspend fun addMessage(conversationId: Long, role: String, text: String): Long {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            conversationId = conversationId,
            role = role,
            text = text,
            timestamp = now
        )
        val messageId = messageDao.insertMessage(message)
        getConversationById(conversationId)?.let { conv ->
            conversationDao.updateConversation(conv.copy(updatedAt = now))
        }
        return messageId
    }

    /**
     * Deletes all messages in a conversation sent after a given timestamp (for editing/rewriting).
     */
    suspend fun deleteMessagesAfter(conversationId: Long, timestamp: Long) {
        messageDao.deleteMessagesAfter(conversationId, timestamp)
    }
}
