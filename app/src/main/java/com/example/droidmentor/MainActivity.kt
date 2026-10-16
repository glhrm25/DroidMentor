package com.example.droidmentor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.droidmentor.active_chat.ActiveChatActivity
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
