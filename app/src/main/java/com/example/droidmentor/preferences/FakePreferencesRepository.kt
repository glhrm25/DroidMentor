package com.example.droidmentor.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePreferencesRepository : PreferencesRepository {

    private val _apiKey = MutableStateFlow<String?>(null)

    override val apiKey: Flow<String?> = _apiKey.asStateFlow()

    override suspend fun saveApiKey(key: String) {
        _apiKey.value = key
    }
}
