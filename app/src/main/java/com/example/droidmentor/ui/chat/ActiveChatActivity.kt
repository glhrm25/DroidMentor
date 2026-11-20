package com.example.droidmentor.ui.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.droidmentor.buildLLogTag
import com.example.droidmentor.preferences.DependenciesContainer
import com.example.droidmentor.ui.theme.DroidMentorTheme


class ActiveChatActivity: ComponentActivity() {

    companion object {
        fun navigateFrom(origin: ComponentActivity, chatId: Long? = null) {
            val msg = Intent(origin, ActiveChatActivity::class.java).apply {
                putExtra(ActiveChatViewModel.CHAT_ID_KEY, chatId)
            }
            origin.startActivity(msg)
        }
    }

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    private val viewModel: ActiveChatViewModel by viewModels {
        ActiveChatViewModel.factory(
            chatApiService = dependencies.apiService,
            conversationRepository = dependencies.conversationRepository
        )
    }

    private val logTag = buildLLogTag(this::class.java.simpleName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                ActiveChatScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(logTag, "onStart() on ${hashCode()}")
    }

    override fun onStop() {
        super.onStop()
        Log.v(logTag, "onStop() on ${hashCode()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(logTag, "onDestroy() on ${hashCode()}")
    }

}