package com.example.droidmentor.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.droidmentor.DependencyContainer
import com.example.droidmentor.ui.theme.DroidMentorTheme
import kotlin.time.Duration.Companion.milliseconds
/** cada activity tem um viewmodelStore*/
class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory((application as DependencyContainer).preferencesRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { finish() },
                    onDebouce = 10.milliseconds
                )
            }
        }
    }
}
