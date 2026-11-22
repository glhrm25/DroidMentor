package com.example.droidmentor.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.droidmentor.R
import androidx.compose.runtime.collectAsState
import com.example.droidmentor.Networking.StatusInternet
import com.example.droidmentor.data.model.chat.ChatMessage
import com.example.droidmentor.data.model.chat.Role
import com.example.droidmentor.ui.chat.components.ChatBubble
import com.example.droidmentor.ui.chat.components.ChatErrorBanner
import com.example.droidmentor.ui.chat.components.ChatInputBar
import com.example.droidmentor.ui.chat.components.EmptyChatPlaceholder
import com.example.droidmentor.ui.chat.components.TopBarBanner


/**
 * Stateful composable that acts as the entry point for the chat screen.
 * It connects the [ActiveChatViewModel] to the UI: it observes the ViewModel's
 * state ([ChatUIState]) and forwards the user's actions (send, retry, dismiss
 * error, stop) to the ViewModel, delegating all rendering to the stateless
 * [ActiveChatScreenContent].
 *
 * @param modifier Modifier for layout adjustments on the main container.
 * @param viewModel The ViewModel that manages business logic and the Gemini API state.
 * @param onNavigateBack Callback triggered to navigate back to the previous screen.
 */
@Composable
fun ActiveChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ActiveChatViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val internetStatus by viewModel.isOnline.collectAsState()

    ActiveChatScreenContent(
        messages = state.messages,
        onSendMessage = viewModel::sendMessage,
        onNavigateBack = onNavigateBack,
        status = state.status,
        onRetry =  viewModel::retryLastRequest,
        onDismiss = viewModel::dismissError,
        onStop = viewModel::cancelResponse,
        internetStatus = internetStatus,
        modifier = modifier
    )
}


/**
 * Stateless composable that builds the chat's visual structure.
 * It contains the top app bar, the message list (with automatic scrolling
 * to the latest message), the error dialog shown when [status] is an error,
 * and the bottom input bar, whose action button switches between send and stop
 * depending on [status].
 *
 * @param messages The current list of messages in the chat history.
 * @param onSendMessage Callback triggered when the user sends a new message.
 * @param onNavigateBack Callback triggered to navigate back to the previous screen.
 * @param status The current state of the chat (idle, loading, receiving or error).
 * It determines whether the stop button is shown and whether sending is allowed.
 * @param onRetry Callback invoked to resend the last message after an error occurs.
 * @param onDismiss Callback invoked to clear the current error state and hide the error message.
 * @param onStop Callback invoked when the user taps the stop button to cancel the
 * response being generated.
 * @param modifier Modifier for the Scaffold root.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveChatScreenContent(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onNavigateBack: () -> Unit,
    status: ChatStatus,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onStop: () -> Unit,
    internetStatus: StatusInternet,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var inputText by remember { mutableStateOf("") }
    val lastMessageLength = messages.lastOrNull()?.text?.length ?: 0

    val isWaiting = status is ChatStatus.Loading
    val isReceiving = status is ChatStatus.Receiving
    val isGenerating = isWaiting || isReceiving

    LaunchedEffect(internetStatus) {
        if (internetStatus is StatusInternet.StatusOff) {
            snackbarHostState.showSnackbar(
                message = "Sem ligação à Internet. As mensagens não serão enviadas.",
                duration = SnackbarDuration.Short
            )
        }
    }

    // Scrolls to the bottom everytime there's a new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // TODO: Rever isto (scroll automatico conforme vai chegando a mensagem)
    LaunchedEffect(lastMessageLength, status) {
        // !listState.isScrollInProgress -> User is not scrolling
        if (isReceiving && !listState.isScrollInProgress) {
            // scrollOffset = Int.MAX_VALUE guarantees that the UI follows the new messages, even if it leaves the screen
            withFrameNanos { }
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems > 0) {
                listState.scrollToItem(totalItems - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBarBanner(stringResource(R.string.app_name) ,onNavigateBack ) } ,
        bottomBar = {
            Column{
                if (status is ChatStatus.Error) {
                    ChatErrorBanner(
                        message = stringResource(id = status.messageId, status.detailedMessage),
                        onRetry = onRetry,
                        onDismiss = onDismiss
                    )
                }

                ChatInputBar(
                    text = inputText,
                    onTextChanged = { inputText = it },
                    sendEnabled = (status is ChatStatus.Idle) && (internetStatus is StatusInternet.StatusOn),
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    onStop = onStop,
                    stopEnabled = isGenerating
                )
            }
        }
    ) { innerPadding ->        // space left between top bar and bottom bar
        Box(
            modifier = modifier
        ) {
            if (messages.isEmpty()) {
                EmptyChatPlaceholder(modifier = Modifier.align(Alignment.Center))
            }

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

                if (isWaiting) {
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

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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
            status = ChatStatus.Idle,
            onSendMessage = { msg ->
                dummyMessages.add(ChatMessage(msg, Role.USER))
                dummyMessages.add(ChatMessage("This is a preview of an answer from the mentor", Role.MODEL))
            },
            onRetry = {},
            onDismiss = {},
            onNavigateBack = {},
            onStop = {},
            internetStatus = StatusInternet.StatusOn
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
            status = ChatStatus.Loading,
            onSendMessage = { },
            onRetry = {},
            onDismiss = {},
            onNavigateBack = {},
            onStop = {},
            internetStatus = StatusInternet.StatusOn
        )
    }
}

@Preview(showBackground = true, name = "Chat error")
@Composable
fun ActiveChatScreenErrorPreview() {
    MaterialTheme {
        val dummyMessages = remember {
            mutableStateListOf(
                ChatMessage("Hello mentor!", Role.USER)
            )
        }

        ActiveChatScreenContent(
            messages = dummyMessages,
            status = ChatStatus.Error(
                messageId = R.string.active_chat_default_error_message,
                detailedMessage = "Não foi possível obter resposta. Verifica a ligação."
            ),
            onSendMessage = {},
            onNavigateBack = {},
            onRetry = {},
            onDismiss = {},
            onStop = {},
            internetStatus = StatusInternet.StatusOff
        )
    }
}