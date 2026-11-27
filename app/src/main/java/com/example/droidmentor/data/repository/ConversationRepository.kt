package com.example.droidmentor.data.repository

import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity
import com.example.droidmentor.data.model.ConversationSnapshot
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
     * The new conversation becomes the single active session (any previous one is deactivated),
     * so that the app can resume it on the next launch if it is not explicitly closed.
     */
    suspend fun createConversation(title: String): Long

    /**
     * Updates an existing conversation.
     */
    suspend fun updateConversation(conversation: ConversationEntity)

    /**
     * Deletes a conversation and all of its messages.
     *
     * @return A [ConversationSnapshot] of the removed data, usable with [restoreConversation]
     * to undo the removal, or `null` if no conversation with the given [id] exists.
     */
    suspend fun deleteConversation(id: Long): ConversationSnapshot?

    /**
     * Restores a previously deleted conversation and its messages, keeping the original IDs.
     * The restored conversation is never marked as the active session.
     */
    suspend fun restoreConversation(snapshot: ConversationSnapshot)

    /**
     * Deletes every conversation and all of their messages.
     */
    suspend fun deleteAllConversations()

    /**
     * Sets a conversation as the single active session.
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
     * Replaces the text of a message, keeping its position in the conversation, and updates the
     * conversation's updatedAt timestamp. Meant for rewriting a message: the messages that follow
     * it are not touched, so they must be removed separately (they are no longer valid).
     * @param messageId The identifier of the message (as returned by [addMessage]).
     * @param text The new text of the message.
     * @return Whether the message was updated (false if there is no such message).
     */
    suspend fun updateMessage(messageId: Long, text: String): Boolean

    /**
     * Deletes a single message.
     * @param messageId The identifier of the message (as returned by [addMessage]).
     * @return Whether the message was deleted (false if there is no such message).
     */
    suspend fun deleteMessage(messageId: Long): Boolean

    /**
     * Deletes a message along with every message that follows it in the same conversation,
     * in the order given by [getMessages]. Meant for rewriting a message: the rewritten message
     * and the replies to it are removed, and the new version is then added with [addMessage].
     * @param messageId The identifier of the first message to delete (as returned by [addMessage]).
     * @return The number of deleted messages (0 if there is no such message).
     */
    suspend fun deleteMessagesFrom(messageId: Long): Int

    /**
     * Deletes all messages in a conversation sent after a given timestamp (for editing/rewriting).
     */
    suspend fun deleteMessagesAfter(conversationId: Long, timestamp: Long)
}
