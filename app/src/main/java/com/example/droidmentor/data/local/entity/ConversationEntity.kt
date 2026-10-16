package com.example.droidmentor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a chat conversation session.
 *
 * @property id Unique identifier for the conversation (auto-generated).
 * @property title Title or summary of the conversation thread.
 * @property createdAt Timestamp in milliseconds when the conversation was initiated.
 * @property updatedAt Timestamp in milliseconds of the most recent message or update.
 * @property isActive Flag indicating whether this session is currently active/open.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = false
)
