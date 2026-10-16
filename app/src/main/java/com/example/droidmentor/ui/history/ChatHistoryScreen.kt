package com.example.droidmentor.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.droidmentor.R
import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.ui.theme.DroidMentorTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stateful composable for the Chat History screen.
 * Connects the [ChatHistoryViewModel] state to the stateless visual content.
 *
 * @param viewModel ViewModel providing the list of conversations and delete actions.
 * @param onNavigateBack Callback invoked to return to the previous screen.
 * @param onSelectConversation Callback invoked when a conversation is tapped.
 * @param onNewConversation Callback invoked to initiate a new conversation.
 * @param modifier Modifier for styling the layout root.
 */
@Composable
fun ChatHistoryScreen(
    viewModel: ChatHistoryViewModel,
    onNavigateBack: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onNewConversation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatHistoryContent(
        conversations = uiState.conversations,
        isLoading = uiState.isLoading,
        onNavigateBack = onNavigateBack,
        onSelectConversation = onSelectConversation,
        onNewConversation = onNewConversation,
        onDeleteConversation = { id -> viewModel.deleteConversation(id) },
        modifier = modifier
    )
}

/**
 * Stateless composable displaying the list of saved conversations or an empty state.
 *
 * @param conversations List of conversation entities to display.
 * @param isLoading Whether data is currently loading.
 * @param onNavigateBack Callback invoked when tapping the back button.
 * @param onSelectConversation Callback invoked when tapping a conversation item.
 * @param onNewConversation Callback invoked when tapping the new conversation action.
 * @param onDeleteConversation Callback invoked when confirming deletion of a conversation.
 * @param modifier Modifier for root container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryContent(
    conversations: List<ConversationEntity>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onNewConversation: () -> Unit,
    onDeleteConversation: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var conversationToDelete by remember { mutableStateOf<ConversationEntity?>(null) }

    // Confirmation dialog for conversation removal
    conversationToDelete?.let { conversation ->
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = { Text(stringResource(R.string.delete_conversation_title)) },
            text = { Text(stringResource(R.string.delete_conversation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteConversation(conversation.id)
                        conversationToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_history_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewConversation,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_chat_button_desc)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                conversations.isEmpty() -> {
                    EmptyHistoryView(
                        onStartChat = onNewConversation,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = conversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationItemCard(
                                conversation = conversation,
                                onClick = { onSelectConversation(conversation.id) },
                                onDeleteClick = { conversationToDelete = conversation }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Component representing a single conversation item card in the history list.
 */
@Composable
fun ConversationItemCard(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedDate = remember(conversation.updatedAt) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        dateFormat.format(Date(conversation.updatedAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_button_desc),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Visual empty state view shown when no conversation histories exist.
 */
@Composable
fun EmptyHistoryView(
    onStartChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.chat_history_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.chat_history_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onStartChat,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.chat_history_new_chat_button))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatHistoryScreenEmptyPreview() {
    DroidMentorTheme {
        ChatHistoryContent(
            conversations = emptyList(),
            isLoading = false,
            onNavigateBack = {},
            onSelectConversation = {},
            onNewConversation = {},
            onDeleteConversation = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatHistoryScreenPopulatedPreview() {
    DroidMentorTheme {
        val sampleConversations = listOf(
            ConversationEntity(
                id = 1,
                title = "Understanding Jetpack Compose State",
                createdAt = System.currentTimeMillis() - 3600000,
                updatedAt = System.currentTimeMillis() - 1800000
            ),
            ConversationEntity(
                id = 2,
                title = "Room Database Migrations & KSP",
                createdAt = System.currentTimeMillis() - 86400000,
                updatedAt = System.currentTimeMillis() - 80000000
            ),
            ConversationEntity(
                id = 3,
                title = "Ktor Client Setup and ContentNegotiation",
                createdAt = System.currentTimeMillis() - 172800000,
                updatedAt = System.currentTimeMillis() - 170000000
            )
        )

        ChatHistoryContent(
            conversations = sampleConversations,
            isLoading = false,
            onNavigateBack = {},
            onSelectConversation = {},
            onNewConversation = {},
            onDeleteConversation = {}
        )
    }
}
