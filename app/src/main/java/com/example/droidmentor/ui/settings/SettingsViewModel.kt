package com.example.droidmentor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.droidmentor.preferences.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.Flow

sealed class StateApi{
    open val apiKey = ""
     data object IdleApiKey: StateApi()
     class LoadingApiKey (override val apiKey:String ): StateApi()
     class ValueApiKey(override val apiKey: String): StateApi()

}
class SettingsViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {

    private val _apiKey = MutableStateFlow<StateApi>(StateApi.IdleApiKey )
    val apiKey: StateFlow<StateApi> = _apiKey.asStateFlow()

    init {
        viewModelScope.launch {
            _apiKey.value = StateApi.LoadingApiKey(_apiKey.value.apiKey)
            val key = repository.apiKey.first()
            _apiKey.value = if ( key != null ) StateApi.ValueApiKey(key) else StateApi.IdleApiKey
        }
    }


    fun updateApiKey(newKey: String) {
        _apiKey.value = StateApi.ValueApiKey(newKey)
    }

    fun saveApiKey() {
        viewModelScope.launch {
            repository.saveApiKey(_apiKey.value.apiKey)
        }
    }

    @Suppress("UNCHECKED_CAST")
    /** Ensina a framework como instanciar parametros no viewmodel*/
    class Factory(private val repository: PreferencesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
