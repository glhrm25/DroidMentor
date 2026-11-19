package com.example.droidmentor.ui.chat.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.droidmentor.R

/**
 * Bottom bar where the user types and sends messages.
 * It automatically handles the keyboard rising ([imePadding]) and swaps the
 * action button depending on whether a response is being generated: a send
 * button when idle, and a stop button while the model is generating.
 *
 * @param text The text currently typed in the input field.
 * @param onTextChanged Callback triggered whenever the typed text changes.
 * @param onSend Callback triggered when the user taps the send button.
 * @param sendEnabled Whether sending is currently allowed (e.g. false while an error
 * is pending). The send button is also disabled when [text] is blank.
 * @param onStop Callback triggered when the user taps the stop button to cancel
 * the response being generated.
 * @param isGenerating Whether the model is currently generating a response. When true,
 * the stop button is shown instead of the send button.
 */
@Composable
fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    onStop: () -> Unit,
    isGenerating: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding(),           // Pushes the bar up when the keyboard is open
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.chat_textBar_placeHolder)) },
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,        // The text box grows vertically if the text is too long
        )

        Spacer(modifier = Modifier.width(12.dp))

        AnimatedContent(targetState = isGenerating) { generating ->
            if (generating) {
                IconButton(
                    onClick = onStop,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = stringResource(R.string.message_stop_button_desc)
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank() && sendEnabled,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.message_send_button_desc)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Input Bar - Idle")
@Composable
private fun ChatInputBarActivePreview() {
    var inputText by remember { mutableStateOf("") }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ChatInputBar(
                text = inputText,
                onTextChanged = { inputText = it },
                onSend = {},
                sendEnabled = inputText.isNotBlank(),
                onStop = {},
                isGenerating = false
            )
        }
    }
}

@Preview(showBackground = true, name = "Input Bar - Loading")
@Composable
private fun ChatInputBarLoadingPreview() {
    var inputText by remember { mutableStateOf("") }

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ChatInputBar(
                text = inputText,
                onTextChanged = { inputText = it },
                onSend = {},
                sendEnabled = false,
                onStop = {},
                isGenerating = true
            )
        }
    }
}
