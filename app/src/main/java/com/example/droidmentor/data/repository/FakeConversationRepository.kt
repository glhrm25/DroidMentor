package com.example.droidmentor.data.repository

import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [ConversationRepository] for tests and previews.
 * Mimics the Room implementation, including the cascading removal of messages.
 */
class FakeConversationRepository : ConversationRepository {

    private val conversations = MutableStateFlow<List<ConversationEntity>>(emptyList())
    private val messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    private var nextConversationId = 1L
    private var nextMessageId = 1L

    override val allConversations: Flow<List<ConversationEntity>> =
        conversations.map { list -> list.sortedByDescending { it.updatedAt } }

    override suspend fun getConversationById(id: Long): ConversationEntity? =
        conversations.value.firstOrNull { it.id == id }

    override suspend fun getActiveConversation(): ConversationEntity? =
        conversations.value.firstOrNull { it.isActive }

    override suspend fun createConversation(title: String): Long {
        val now = System.currentTimeMillis()
        val conversation = ConversationEntity(
            id = nextConversationId++,
            title = title,
            createdAt = now,
            updatedAt = now,
            isActive = true
        )
        conversations.value = conversations.value + conversation
        return conversation.id
    }

    override suspend fun updateConversation(conversation: ConversationEntity) {
        conversations.value =
            conversations.value.map { if (it.id == conversation.id) conversation else it }
    }

    override suspend fun deleteConversation(id: Long) {
        conversations.value = conversations.value.filterNot { it.id == id }
        messages.value = messages.value.filterNot { it.conversationId == id }
    }

    override suspend fun setActiveConversation(id: Long) {
        conversations.value = conversations.value.map { it.copy(isActive = it.id == id) }
    }

    override suspend fun clearActiveConversation() {
        conversations.value = conversations.value.map { it.copy(isActive = false) }
    }

    override fun getMessages(conversationId: Long): Flow<List<MessageEntity>> =
        messages.map { list ->
            list.filter { it.conversationId == conversationId }.sortedBy { it.timestamp }
        }

    override suspend fun addMessage(conversationId: Long, role: String, text: String): Long {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            id = nextMessageId++,
            conversationId = conversationId,
            role = role,
            text = text,
            timestamp = now
        )
        messages.value = messages.value + message
        getConversationById(conversationId)?.let { updateConversation(it.copy(updatedAt = now)) }
        return message.id
    }

    override suspend fun deleteMessagesAfter(conversationId: Long, timestamp: Long) {
        messages.value = messages.value.filterNot {
            it.conversationId == conversationId && it.timestamp > timestamp
        }
    }
}
