package com.example.droidmentor.data.model

import com.example.droidmentor.data.local.entity.ConversationEntity

/**
 * Domain model representing a conversation session.
 *
 * @property id The unique identifier of the conversation.
 * @property title The title or preview subject of the conversation.
 * @property createdAt Timestamp when the conversation was initiated.
 * @property updatedAt Timestamp when the conversation was last updated.
 * @property isActive Whether this session is currently active.
 */
data class Conversation(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean
)

/**
 * Maps a Room [ConversationEntity] to domain [Conversation].
 */
fun ConversationEntity.toDomain() = Conversation(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isActive = isActive
)
