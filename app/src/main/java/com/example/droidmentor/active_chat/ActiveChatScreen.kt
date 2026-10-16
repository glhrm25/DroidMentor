package com.example.droidmentor.active_chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.droidmentor.R


/**
 * Stateful composable that acts as the entry point for the chat screen.
 * It is responsible for connecting the [ActiveChatViewModel] to the UI, observing
 * data streams (StateFlow), and passing them to the stateless visual component.
 *
 * @param modifier Modifier for layout adjustments on the main container.
 * @param viewModel The ViewModel that manages business logic and the Gemini API state.
 */
@Composable
fun ActiveChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ActiveChatViewModel,
    onNavigateBack: () -> Unit = {}
) {
    ActiveChatScreenContent(
        messages = viewModel.messages.collectAsState().value,
        onSendMessage = { msg -> viewModel.sendMessage(msg) },
        onNavigateBack = onNavigateBack,
        isLoading = viewModel.isLoading.collectAsState().value,
        modifier = modifier
    )
}


/**
 * Stateless composable that builds the chat's visual structure.
 * It contains the top app bar, the message list (with automatic scrolling
 * to the latest message), and the bottom input bar.
 *
 * @param messages The current list of messages in the chat history.
 * @param onSendMessage Callback triggered when the user sends a new message.
 * @param onNavigateBack Callback triggered to navigate back to the previous screen.
 * @param isLoading Indicates whether the app is waiting for an API response (shows the spinning icon).
 * @param modifier Modifier for the Scaffold root.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveChatScreenContent(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    // Controls the list state so we can scroll ???
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }

    // Scrolls to the bottom everytime there's a new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigateBack() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                text = inputText,
                onTextChanged = { inputText = it },
                onSendClicked = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                isLoading = isLoading
            )
        }
    ) { innerPadding ->        // space left between top bar and bottom bar
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)       // space between the message bubbles
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


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


/**
 * Bottom bar where the user types and sends messages.
 * It automatically handles the keyboard rising ([imePadding]) and manages
 * the send button's enabled/disabled state.
 *
 * @param text The text currently typed in the input field.
 * @param onTextChanged Callback triggered whenever the typed text changes.
 * @param onSendClicked Callback triggered when the user taps the send button.
 * @param isLoading Determines if the button should be disabled while the model generates a response.
 */
@Composable
fun ChatInputBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    isLoading: Boolean
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
            maxLines = 4        // The text box grows vertically if the text is too long
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onSendClicked,
            enabled = text.isNotBlank() && !isLoading,
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

@Preview(showBackground = true, name = "Normal chat")
@Composable
fun ActiveChatScreenPreview() {
    MaterialTheme {
        val dummyMessages = remember {
            mutableStateListOf(
                ChatMessage("Hi mentor!", Role.USER),
                ChatMessage("Hi user!", Role.MODEL)
            )
        }

        ActiveChatScreenContent(
            messages = dummyMessages,
            isLoading = false,
            onSendMessage = { msg ->
                dummyMessages.add(ChatMessage(msg, Role.USER))
                dummyMessages.add(ChatMessage("This is a preview of an answer from the mentor", Role.MODEL))
            },
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Chat loading")
@Composable
fun ActiveChatScreenLoadingPreview() {
    MaterialTheme {
        val dummyMessages = remember {
            mutableStateListOf(
                ChatMessage("Hello mentor!", Role.USER)
            )
        }

        ActiveChatScreenContent(
            messages = dummyMessages,
            isLoading = true,
            onSendMessage = { },
            onNavigateBack = {}
        )
    }
}