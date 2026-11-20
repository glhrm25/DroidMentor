package com.example.droidmentor.preferences

import com.example.droidmentor.data.repository.ConversationRepository
import com.example.droidmentor.data.services.chat.ChatAPIService

interface DependenciesContainer {
    val apiService: ChatAPIService
    val preferencesRepository: PreferencesRepository
    val conversationRepository: ConversationRepository
}