package com.example.droidmentor.ui.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.droidmentor.ui.theme.DroidMentorTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                AboutScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
