package com.example.droidmentor.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.repository.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the Chat History screen.
 *
 * @property conversations List of saved conversations from the database.
 * @property isLoading Indicates whether conversations are currently being loaded.
 */
data class ChatHistoryUiState(
    val conversations: List<ConversationEntity> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel responsible for managing conversational history state and actions.
 * Observes data changes from [ConversationRepository] and exposes a reactive [StateFlow].
 *
 * @property repository Repository providing database access to conversation entities.
 */
class ChatHistoryViewModel(
    private val repository: ConversationRepository
) : ViewModel() {

    /**
     * UI state stream observing saved conversations from the database.
     */
    val uiState: StateFlow<ChatHistoryUiState> = repository.allConversations
        .map { list -> ChatHistoryUiState(conversations = list, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatHistoryUiState(isLoading = true)
        )

    /**
     * Deletes a conversation and its messages from the database.
     *
     * @param id The unique identifier of the conversation to delete.
     */
    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)
        }
    }

    /**
     * Creates a new conversation and invokes the callback with the new conversation ID.
     *
     * @param title The initial title for the conversation.
     * @param onCreated Callback invoked with the generated conversation ID.
     */
    fun createConversation(title: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createConversation(title)
            onCreated(id)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ConversationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatHistoryViewModel::class.java)) {
                return ChatHistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
