package com.example.droidmentor.data.model

import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity

/**
 * Full copy of a conversation (header and messages) taken right before it is removed,
 * so that the removal can be undone by restoring it with the same identifiers.
 *
 * @property conversation The removed conversation row.
 * @property messages The messages that belonged to the conversation, in chronological order.
 */
data class ConversationSnapshot(
    val conversation: ConversationEntity,
    val messages: List<MessageEntity>
)
