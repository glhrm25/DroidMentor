package com.example.droidmentor.data.services.chat

import kotlinx.coroutines.flow.Flow

interface ChatAPIService {
    fun generateContentStream(history: List<Content>): Flow<String>
}