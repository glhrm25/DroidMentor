package com.example.droidmentor.data.repository

import com.example.droidmentor.data.local.dao.ConversationDao
import com.example.droidmentor.data.local.dao.MessageDao
import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity
import com.example.droidmentor.data.model.ConversationSnapshot
import kotlinx.coroutines.flow.Flow

/**
 * [ConversationRepository] implementation backed by the Room database.
 *
 * @property conversationDao DAO for conversation operations.
 * @property messageDao DAO for message operations.
 */
class RoomConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : ConversationRepository {

    override val allConversations: Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    override suspend fun getConversationById(id: Long): ConversationEntity? {
        return conversationDao.getConversationById(id)
    }

    override suspend fun getActiveConversation(): ConversationEntity? {
        return conversationDao.getActiveConversation()
    }

    override suspend fun createConversation(title: String): Long {
        conversationDao.clearActiveConversation()
        val now = System.currentTimeMillis()
        val conversation = ConversationEntity(
            title = title,
            createdAt = now,
            updatedAt = now,
            isActive = true
        )
        return conversationDao.insertConversation(conversation)
    }

    override suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }

    override suspend fun deleteConversation(id: Long): ConversationSnapshot? {
        val conversation = conversationDao.getConversationById(id) ?: return null
        val messages = messageDao.getMessagesForConversationSync(id)
        conversationDao.deleteConversationById(id)
        return ConversationSnapshot(conversation, messages)
    }

    override suspend fun restoreConversation(snapshot: ConversationSnapshot) {
        conversationDao.insertConversation(snapshot.conversation.copy(isActive = false))
        if (snapshot.messages.isNotEmpty()) {
            messageDao.insertMessages(snapshot.messages)
        }
    }

    override suspend fun deleteAllConversations() {
        conversationDao.deleteAllConversations()
    }

    override suspend fun setActiveConversation(id: Long) {
        conversationDao.clearActiveConversation()
        conversationDao.setActiveConversation(id)
    }

    override suspend fun clearActiveConversation() {
        conversationDao.clearActiveConversation()
    }

    override fun getMessages(conversationId: Long): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    override suspend fun addMessage(conversationId: Long, role: String, text: String): Long {
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

    override suspend fun deleteMessage(messageId: Long): Boolean {
        return messageDao.deleteMessageById(messageId) > 0
    }

    override suspend fun deleteMessagesFrom(messageId: Long): Int {
        return messageDao.deleteMessagesFrom(messageId)
    }

    override suspend fun deleteMessagesAfter(conversationId: Long, timestamp: Long) {
        messageDao.deleteMessagesAfter(conversationId, timestamp)
    }
}
