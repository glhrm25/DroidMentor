package com.example.droidmentor.ui.chat

import androidx.annotation.StringRes
import com.example.droidmentor.data.model.chat.ChatMessage

sealed class ChatStatus {
    data object Idle : ChatStatus()
    data object Loading : ChatStatus()
    data object Receiving : ChatStatus()
    data class Error(
        @StringRes val messageId: Int,
        val detailedMessage: String
    ): ChatStatus()
}

data class ChatUIState(
    val messages: List<ChatMessage> = emptyList(),
    val status: ChatStatus = ChatStatus.Idle
)