package com.example.droidmentor.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val apiKey: Flow<String?>
    suspend fun saveApiKey(key: String)
}