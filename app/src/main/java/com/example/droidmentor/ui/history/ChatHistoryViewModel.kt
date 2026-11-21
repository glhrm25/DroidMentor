package com.example.droidmentor.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.model.ConversationSnapshot
import com.example.droidmentor.data.repository.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state for the Chat History screen.
 *
 * @property conversations List of saved conversations from the database.
 * @property isLoading Indicates whether conversations are currently being loaded.
 * @property recentlyDeleted The last removed conversation while its removal can still be undone,
 * or `null` when there is nothing to undo.
 */
data class ChatHistoryUiState(
    val conversations: List<ConversationEntity> = emptyList(),
    val isLoading: Boolean = false,
    val recentlyDeleted: ConversationEntity? = null
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
     * Snapshot of the last removed conversation, kept while the user may still undo the removal.
     */
    private val lastDeleted = MutableStateFlow<ConversationSnapshot?>(null)

    /**
     * UI state stream observing saved conversations from the database.
     */
    val uiState: StateFlow<ChatHistoryUiState> =
        combine(repository.allConversations, lastDeleted) { list, deleted ->
            ChatHistoryUiState(
                conversations = list,
                isLoading = false,
                recentlyDeleted = deleted?.conversation
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatHistoryUiState(isLoading = true)
        )

    /**
     * Deletes a conversation and its messages from the database.
     * The removal can be reverted with [undoDelete] until [dismissUndo] is called.
     *
     * @param id The unique identifier of the conversation to delete.
     */
    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            repository.deleteConversation(id)?.let { snapshot -> lastDeleted.value = snapshot }
        }
    }

    /**
     * Restores the last deleted conversation, including all of its messages.
     */
    fun undoDelete() {
        val snapshot = lastDeleted.value ?: return
        lastDeleted.value = null
        viewModelScope.launch {
            repository.restoreConversation(snapshot)
        }
    }

    /**
     * Discards the possibility of undoing the last removal (e.g. the undo message timed out).
     */
    fun dismissUndo() {
        lastDeleted.value = null
    }

    /**
     * Deletes every saved conversation. This action cannot be undone.
     */
    fun deleteAllConversations() {
        lastDeleted.value = null
        viewModelScope.launch {
            repository.deleteAllConversations()
        }
    }

    /**
     * Marks the given conversation as the active session before opening it, so that it is
     * resumed automatically if the app is terminated while the conversation is open.
     *
     * @param id The conversation to open.
     * @param onReady Callback invoked (on the main thread) once the session has been marked.
     */
    fun openConversation(id: Long, onReady: (Long) -> Unit) {
        viewModelScope.launch {
            repository.setActiveConversation(id)
            onReady(id)
        }
    }

    /**
     * Closes the active session. Called when the user explicitly navigates back from the
     * Active Chat screen to this screen.
     */
    fun endActiveSession() {
        viewModelScope.launch {
            repository.clearActiveConversation()
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
