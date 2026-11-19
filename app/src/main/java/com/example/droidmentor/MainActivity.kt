package com.example.droidmentor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.droidmentor.ui.chat.ActiveChatActivity
import com.example.droidmentor.ui.about.AboutActivity
import com.example.droidmentor.ui.history.ChatHistoryActivity
import com.example.droidmentor.ui.settings.SettingsActivity

import com.example.droidmentor.ui.theme.DroidMentorTheme
import com.example.droidmentor.ui.title.TitleScreen

const val APP_TAG = "DroidMentorApp"
fun buildLLogTag(className: String) = "$APP_TAG.$className"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                TitleScreen(
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
