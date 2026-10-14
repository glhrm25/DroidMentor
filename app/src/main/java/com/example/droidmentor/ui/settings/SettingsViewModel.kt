package com.example.droidmentor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.droidmentor.preferences.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    init {
        viewModelScope.launch {
            val key = repository.apiKey.first()
            _apiKey.value = key ?: ""
        }
    }

    fun updateApiKey(newKey: String) {
        _apiKey.value = newKey
    }

    fun saveApiKey() {
        viewModelScope.launch {
            repository.saveApiKey(_apiKey.value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: PreferencesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
