package com.example.droidmentor.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.milliseconds
/** Base de dados persistente, guarda objetos simples chave valor
 * */
class DataStorePreferencesRepository(private val dataStore: DataStore<Preferences>) : PreferencesRepository {

    companion object {
        private val API_KEY = stringPreferencesKey("gemini_api_key")
    }

    override val apiKey: Flow<String?> = dataStore.data.map { preferences ->
        preferences[API_KEY]
    }

    override suspend fun saveApiKey(key: String) {
        dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }
}
