package com.example.droidmentor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an individual chat message in a conversation.
 *
 * @property id Unique identifier for the message (auto-generated).
 * @property conversationId ID of the parent [ConversationEntity].
 * @property role Sender role of the message (e.g. "USER" or "MODEL").
 * @property text Text content of the message.
 * @property timestamp Timestamp in milliseconds when the message was sent/received.
 * @property imageUri Optional URI pointing to locally stored image file (as per assignment requirement).
 */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val role: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null
)
