package com.example.droidmentor.active_chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.droidmentor.buildLLogTag
import com.example.droidmentor.ui.theme.DroidMentorTheme


class ActiveChatActivity: ComponentActivity() {

    companion object {
        fun navigateFrom(origin: ComponentActivity) {
            val msg = Intent(origin, ActiveChatActivity::class.java)
            origin.startActivity(msg)
        }
    }

    private val viewModel by viewModels<ActiveChatViewModel>()

    private val logTag = buildLLogTag(this::class.java.simpleName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()
        setContent {
            DroidMentorTheme {
                ActiveChatScreen(
                    viewModel = viewModel
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