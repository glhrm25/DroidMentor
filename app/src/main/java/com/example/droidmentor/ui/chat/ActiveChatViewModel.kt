package com.example.droidmentor.ui.chat

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.droidmentor.data.services.chat.Content
import com.example.droidmentor.data.services.chat.GeminiApiService
import com.example.droidmentor.data.model.chat.ChatMessage
import com.example.droidmentor.data.model.chat.Role
import com.example.droidmentor.buildLLogTag
import com.example.droidmentor.R
import com.example.droidmentor.data.model.chat.toChatMessage
import com.example.droidmentor.data.repository.ConversationRepository
import com.example.droidmentor.data.services.chat.ChatAPIService
import com.example.droidmentor.ui.history.ChatHistoryActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * ViewModel responsible for managing the state and business logic of the active chat screen.
 * Maintains conversation history in memory, updates UI state flows, and coordinates
 * asynchronous request processing with [GeminiApiService].
 */
class ActiveChatViewModel(
    private val apiService: ChatAPIService,
    private val conversationRepository: ConversationRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val logTag = buildLLogTag(this::class.java.simpleName)

    private val chatId get() = savedStateHandle.get<Long>(CHAT_ID_KEY)
    private val _state = MutableStateFlow(ChatUIState())
    val state: StateFlow<ChatUIState> = _state.asStateFlow()

    private var workingJob: Job? = null

    init {
        Log.v(logTag, "init on ${hashCode()}")
        viewModelScope.launch {
            _state.update { current -> current.copy(messages = getStoredMessages()) }
        }
    }

    private suspend fun getStoredMessages(): List<ChatMessage> =
        chatId?.let { chatId ->
            conversationRepository.getMessages(chatId)
                .first()
                .map { msg ->
                    msg.toChatMessage()
                }
        } ?: emptyList()


    /**
     * Sends a new user message to the chat.
     * Appends the user's message to the local state and starts a response generation through [startGeneration].
     *
     * @param userText The raw text string entered by the user.
     */
    fun sendMessage(userText: String) {
        if (_state.value.status !is ChatStatus.Idle) {
            return
        }

        val newMessage = ChatMessage(text = userText, Role.USER)
        _state.update { current ->
            current.copy(
                messages = current.messages + newMessage
            )
        }

        saveMessage(newMessage)

        startGeneration()
    }

    private fun saveMessage(msg: ChatMessage) {
        viewModelScope.launch {
            if (chatId == null) {
                savedStateHandle[CHAT_ID_KEY] = conversationRepository.createConversation("New Conversation")
            }

            chatId?.let { conversationRepository.addMessage(it, msg.sender.toString(), msg.text) }
        }
    }

    /**
     * Requests a response from the mentor for the current conversation.
     * Sets the status to [ChatStatus.Loading], converts the chat history into
     * API-compatible [Content] objects and collects the response stream from
     * [GeminiApiService] in a coroutine stored in [workingJob].
     *
     * The first chunk creates the model's message and moves the status to
     * [ChatStatus.Receiving]; each following chunk is appended to that message.
     * If the stream fails, the status becomes [ChatStatus.Error] (any partial
     * response is kept in the history). When the coroutine ends, whether normally or by cancellation,
     * the status returns to [ChatStatus.Idle] and [workingJob] is cleared, unless it is an error.
     */
    private fun startGeneration() {
        if (_state.value.status !is ChatStatus.Idle) {
            return
        }

        _state.update { current ->
            current.copy(status = ChatStatus.Loading)
        }

        // Maps domain messages to API request history objects
        val conversationHistory = _state.value.messages.map { msg -> msg.toContent() }

        // Asynchronous request to the Gemini API
        workingJob = viewModelScope.launch {
            try {
                apiService.generateContentStream(conversationHistory)
                    .catch { e ->
                        _state.update { current ->
                            current.copy(
                                status = ChatStatus.Error(
                                    R.string.active_chat_default_error_message,
                                    e.message ?: ""
                                )
                            )
                        }
                    }
                    .collect { chunk ->
                        val observedState = _state.value
                        if (observedState.status is ChatStatus.Loading) { // first chunk
                            _state.emit(
                                value = _state.value.copy(
                                    messages = observedState.messages + ChatMessage(text = chunk, Role.MODEL),
                                    status = ChatStatus.Receiving
                                )
                            )
                        } else {
                            _state.update { current ->
                                val lastMessage = current.messages.last()
                                current.copy(
                                    messages = current.messages.dropLast(1) + lastMessage.copy(text = lastMessage.text + chunk),
                                    status = ChatStatus.Receiving
                                )
                            }
                        }
                    }
            } finally {
                if (_state.value.status !is ChatStatus.Error)
                    _state.update { current -> current.copy(status = ChatStatus.Idle) }

                val lastMessage = _state.value.messages.last()
                if (lastMessage.sender == Role.MODEL)       // TODO: REVER ISTO (FAZER ISTO AQUI ??? E SE HOUVER UM RETRY QUE FALHA TAMBÉM?)
                    saveMessage(lastMessage)

                workingJob = null
            }
        }
    }

    /**
     * Clears the current error state, returning the chat to [ChatStatus.Idle].
     * It does not change the message history.
     */
    fun dismissError(){
        if (_state.value.status is ChatStatus.Error) {
            _state.update { current -> current.copy(status = ChatStatus.Idle) }
        }
    }

    /**
     * Retries the last request after an error.
     * If the last message is a (partial) response from the model, it is discarded
     * so that the history ends with the user's message, and a new generation is
     * started through [startGeneration].
     */
    fun retryLastRequest() {
        if (_state.value.status !is ChatStatus.Error) {
            return
        }

        val lastMessage = _state.value.messages.lastOrNull()

        if (lastMessage?.sender == Role.MODEL) {
            _state.update { current ->
                current.copy(messages = current.messages.dropLast(1))
            }
        }

        startGeneration()
    }

    /**
     * Cancels the response currently being generated, if any.
     * Any text already received is kept in the history. The status returns to
     * [ChatStatus.Idle] once the canceled coroutine finishes (see [startGeneration]).
     */
    fun cancelResponse() {
        workingJob?.cancel()
    }

    companion object {
        const val CHAT_ID_KEY = ChatHistoryActivity.EXTRA_CONVERSATION_ID

        fun factory(
            chatApiService: ChatAPIService,
            conversationRepository: ConversationRepository
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { ActiveChatViewModel(chatApiService, conversationRepository, createSavedStateHandle()) }
            }
    }
}
