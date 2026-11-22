package com.example.droidmentor.data.services.chat.fakes

import com.example.droidmentor.data.services.chat.ChatAPIService
import com.example.droidmentor.data.services.chat.Content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

class FakeChatAPIService : ChatAPIService {

    /** Comportamento configurável por teste. Por omissão, a resposta é vazia. */
    var behavior: (List<Content>) -> Flow<String> = { emptyFlow() }

    /** Históricos recebidos, para verificar o que o ViewModel enviou. */
    val receivedHistories = mutableListOf<List<Content>>()

    override fun generateContentStream(history: List<Content>): Flow<String> {
        receivedHistories += history
        return behavior(history)
    }

    fun respondsWith(vararg chunks: String) {
        behavior = { chunks.asFlow() }
    }

    fun failsWith(error: Throwable) {
        behavior = { flow { throw error } }
    }

    fun respondsThenFails(vararg chunks: String, error: Throwable) {
        behavior = { flow { chunks.forEach { emit(it) }; throw error } }
    }
}