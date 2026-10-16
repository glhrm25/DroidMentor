package com.example.droidmentor.ui.history

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.droidmentor.active_chat.ActiveChatActivity
import com.example.droidmentor.data.local.AppDatabase
import com.example.droidmentor.data.repository.ConversationRepository
import com.example.droidmentor.ui.theme.DroidMentorTheme

/**
 * Activity hosting the Chat History screen.
 * Displays previous chat conversations stored in the Room database,
 * provides empty state handling, and enables session removal and creation.
 */
class ChatHistoryActivity : ComponentActivity() {

    companion object {
        const val EXTRA_CONVERSATION_ID = "extra_conversation_id"

        /**
         * Convenience helper to launch [ChatHistoryActivity] from another Activity.
         */
        fun navigateFrom(origin: ComponentActivity) {
            val intent = Intent(origin, ChatHistoryActivity::class.java)
            origin.startActivity(intent)
        }
    }

    private val viewModel: ChatHistoryViewModel by viewModels {
        val database = AppDatabase.getInstance(applicationContext)
        val repository = ConversationRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao()
        )
        ChatHistoryViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                ChatHistoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onSelectConversation = { conversationId ->
                        val intent = Intent(this@ChatHistoryActivity, ActiveChatActivity::class.java).apply {
                            putExtra(EXTRA_CONVERSATION_ID, conversationId)
                        }
                        startActivity(intent)
                    },
                    onNewConversation = {
                        ActiveChatActivity.navigateFrom(this@ChatHistoryActivity)
                    }
                )
            }
        }
    }
}
