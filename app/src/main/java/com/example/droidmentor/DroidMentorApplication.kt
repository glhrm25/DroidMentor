package com.example.droidmentor

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.droidmentor.data.local.AppDatabase
import com.example.droidmentor.data.repository.ConversationRepository
import com.example.droidmentor.data.repository.RoomConversationRepository
import com.example.droidmentor.data.services.chat.ChatAPIService
import com.example.droidmentor.data.services.chat.GeminiApiService
import com.example.droidmentor.data.services.chat.createApiHttpClient
import com.example.droidmentor.preferences.DataStorePreferencesRepository
import com.example.droidmentor.preferences.DependenciesContainer
import com.example.droidmentor.preferences.PreferencesRepository
import io.ktor.client.engine.okhttp.OkHttp


/** Contexto geral da aplicação que irá fazer ponto de ligação com os repositórios*/
class DroidMentorApplication : Application(), DependenciesContainer {
    // Cria uma instância do DataStore e em que name irá ser nome do ficheiro
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "droidmentor_settings")

    override val preferencesRepository: PreferencesRepository by lazy {
        DataStorePreferencesRepository(dataStore)
    }

    override val apiService: ChatAPIService by lazy {
        GeminiApiService(
            client = createApiHttpClient(OkHttp.create()),
            preferencesRepository = preferencesRepository
        )
    }

    override val conversationRepository: ConversationRepository by lazy {
        val database = AppDatabase.getInstance(applicationContext)
        RoomConversationRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao()
        )
    }
}
