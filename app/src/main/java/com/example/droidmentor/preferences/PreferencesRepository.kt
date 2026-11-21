package com.example.droidmentor.preferences

import kotlinx.coroutines.flow.Flow
/**
 * Apikey representa a api para ter as conversações
 * */
interface PreferencesRepository {
    val apiKey: Flow<String?>
    suspend fun saveApiKey(key: String)
}