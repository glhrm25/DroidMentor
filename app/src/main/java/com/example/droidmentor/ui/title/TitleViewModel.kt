package com.example.droidmentor.ui.title

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.droidmentor.data.repository.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * States of the app's startup flow, decided by the Title screen.
 */
sealed interface StartupState {
    /** The app is checking whether a previous session was left open. */
    data object Checking : StartupState

    /** No session to resume: the Title (main menu) is displayed. */
    data object ShowTitle : StartupState

    /**
     * A conversation was left open when the app was terminated and must be resumed.
     *
     * @property conversationId The conversation to reopen in the Active Chat screen.
     */
    data class ResumeConversation(val conversationId: Long) : StartupState
}

/**
 * ViewModel for the Title screen. Implements the automatic redirection on app launch:
 * if the user did not explicitly close the last conversation before the app was terminated,
 * that conversation is reopened instead of showing the main menu.
 *
 * @property repository Repository giving access to the persisted active-session flag.
 */
class TitleViewModel(
    private val repository: ConversationRepository
) : ViewModel() {

    private val _startupState = MutableStateFlow<StartupState>(StartupState.Checking)

    /**
     * The current startup state observed by the UI.
     */
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    /**
     * Decides what to show when the Title screen is created. Only acts once per ViewModel.
     *
     * @param isColdStart `true` when the activity is created from scratch (app launch);
     * `false` when it is being recreated (e.g. configuration change or restored task), in which
     * case the Title is shown directly because the back stack is restored by the system.
     */
    fun resolveStartup(isColdStart: Boolean) {
        if (_startupState.value != StartupState.Checking) return
        if (!isColdStart) {
            _startupState.value = StartupState.ShowTitle
            return
        }
        viewModelScope.launch {
            val active = repository.getActiveConversation()
            _startupState.value =
                if (active != null) StartupState.ResumeConversation(active.id)
                else StartupState.ShowTitle
        }
    }

    /**
     * Must be called after the UI navigated to the resumed conversation, so that the Title
     * is shown when the user comes back.
     */
    fun onRedirectHandled() {
        _startupState.value = StartupState.ShowTitle
    }

    /**
     * Closes the active session. Called when the user explicitly navigates back to the Title.
     */
    fun endActiveSession() {
        viewModelScope.launch {
            repository.clearActiveConversation()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: ConversationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TitleViewModel::class.java)) {
                return TitleViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
