package com.example.droidmentor.ui.chat.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.droidmentor.data.model.chat.ChatMessage
import com.example.droidmentor.data.model.chat.Role

/**
 * Visual component that draws a single message bubble.
 * Its design (alignment, colors, and corner shape) adapts
 * depending on whether the sender is the user or the model (Mentor).
 *
 * @param message The data object containing the message text and the sender's identity.
 */
@Composable
fun ChatBubble(message: ChatMessage) {
    val backgroundColor: Color
    val textColor: Color
    val alignment: Arrangement.Horizontal
    val shape: RoundedCornerShape

    if (message.sender == Role.USER) {
        backgroundColor = MaterialTheme.colorScheme.primary
        textColor = MaterialTheme.colorScheme.onPrimary
        alignment = Arrangement.End
        shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        textColor = MaterialTheme.colorScheme.onSurfaceVariant
        alignment = Arrangement.Start
        shape = RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .background(color = backgroundColor, shape = shape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}


@Preview(showBackground = true, name = "Chat Bubbles")
@Composable
private fun ChatBubblePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChatBubble(
                    message = ChatMessage("Hi Mentor! What is a sealed class?", Role.USER)
                )
                ChatBubble(
                    message = ChatMessage(
                        "It's a class that restrains who can inherit from it.",
                        Role.MODEL
                    )
                )
            }
        }
    }
}