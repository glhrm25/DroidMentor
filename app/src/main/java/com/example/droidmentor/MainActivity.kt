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
import com.example.droidmentor.ui.about.AboutActivity
import com.example.droidmentor.ui.settings.SettingsActivity
import com.example.droidmentor.ui.theme.DroidMentorTheme

const val APP_TAG = "DroidMentorApp"
fun buildLLogTag(className: String) = "$APP_TAG.$className"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Home Screen (Temporary)")
                        Button(
                            onClick = {
                                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Go to Settings")
                        }
                        Button(
                            onClick = {
                                startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Go to About")
                        }
                    }
                }
            }
        }
    }
}
