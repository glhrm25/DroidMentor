package com.example.droidmentor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.droidmentor.ui.chat.ActiveChatActivity
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.droidmentor.preferences.DependenciesContainer
import com.example.droidmentor.ui.about.AboutActivity
import com.example.droidmentor.ui.history.ChatHistoryActivity
import com.example.droidmentor.ui.settings.SettingsActivity
import com.example.droidmentor.ui.theme.DroidMentorTheme
import com.example.droidmentor.ui.title.StartupLoadingScreen
import com.example.droidmentor.ui.title.StartupState
import com.example.droidmentor.ui.title.TitleScreen
import com.example.droidmentor.ui.title.TitleViewModel

const val APP_TAG = "DroidMentorApp"
fun buildLLogTag(className: String) = "$APP_TAG.$className"

class MainActivity : ComponentActivity() {

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    private val viewModel: TitleViewModel by viewModels {
        TitleViewModel.Factory(dependencies.conversationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.resolveStartup(isColdStart = savedInstanceState == null)
        setContent {
            DroidMentorTheme {
                val startupState by viewModel.startupState.collectAsState()
                when (val state = startupState) {
                    StartupState.Checking -> StartupLoadingScreen(isResuming = false)

                    is StartupState.ResumeConversation -> {
                        StartupLoadingScreen(isResuming = true)
                        LaunchedEffect(state.conversationId) {
                            ChatHistoryActivity.resumeConversationFrom(
                                this@MainActivity,
                                state.conversationId
                            )
                            viewModel.onRedirectHandled()
                        }
                    }

                    StartupState.ShowTitle -> TitleScreen(
                        onNavigateToNewChat = {
                            ActiveChatActivity.navigateFrom(this@MainActivity)
                        },
                        onNavigateToHistory = {
                            ChatHistoryActivity.navigateFrom(this@MainActivity)
                        },
                        onNavigateToSettings = {
                            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        },
                        onNavigateToAbout = {
                            startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                        }
                    )
                }
            }
        }
    }

    /**
     * Called when the user comes back to the Title (e.g. presses back on the Active Chat screen).
     * Returning here means the conversation was explicitly left, so the session is closed and
     * will not be resumed on the next launch.
     */
    override fun onRestart() {
        super.onRestart()
        viewModel.endActiveSession()
    }
}
