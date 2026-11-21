package com.example.droidmentor.ui.history

import com.example.droidmentor.MainDispatcherRule
import com.example.droidmentor.data.repository.FakeConversationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChatHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeConversationRepository()

    private suspend fun seedConversationWithMessages(title: String): Long {
        val id = repository.createConversation(title)
        repository.addMessage(id, "USER", "Question about $title")
        repository.addMessage(id, "MODEL", "Answer about $title")
        return id
    }

    private suspend fun conversationIds() = repository.allConversations.first().map { it.id }

    @Test
    fun `deleteConversation removes the conversation and its messages`() = runTest {
        // Arrange
        val keptId = seedConversationWithMessages("Kept")
        val removedId = seedConversationWithMessages("Removed")
        val vm = ChatHistoryViewModel(repository)

        // Act
        vm.deleteConversation(removedId)

        // Assert
        assertEquals(listOf(keptId), conversationIds())
        assertTrue(repository.getMessages(removedId).first().isEmpty())
        assertEquals(2, repository.getMessages(keptId).first().size)
    }

    @Test
    fun `deleteConversation exposes the removed conversation for undo`() = runTest {
        // Arrange
        val id = seedConversationWithMessages("Removed")
        val vm = ChatHistoryViewModel(repository)
        backgroundScope.launch { vm.uiState.collect {} }

        // Act
        vm.deleteConversation(id)

        // Assert
        val state = vm.uiState.first { !it.isLoading }
        assertEquals(id, state.recentlyDeleted?.id)
        assertTrue(state.conversations.isEmpty())
    }

    @Test
    fun `undoDelete restores the conversation and all of its messages`() = runTest {
        // Arrange
        val id = seedConversationWithMessages("Restored")
        val originalMessages = repository.getMessages(id).first()
        val vm = ChatHistoryViewModel(repository)
        vm.deleteConversation(id)

        // Act
        vm.undoDelete()

        // Assert
        assertEquals(listOf(id), conversationIds())
        assertEquals(originalMessages, repository.getMessages(id).first())
        assertFalse(repository.getConversationById(id)!!.isActive)
    }

    @Test
    fun `dismissUndo makes the removal permanent`() = runTest {
        // Arrange
        val id = seedConversationWithMessages("Removed")
        val vm = ChatHistoryViewModel(repository)
        backgroundScope.launch { vm.uiState.collect {} }
        vm.deleteConversation(id)

        // Act
        vm.dismissUndo()
        vm.undoDelete()

        // Assert
        assertNull(vm.uiState.value.recentlyDeleted)
        assertTrue(conversationIds().isEmpty())
    }

    @Test
    fun `deleteConversation with an unknown id changes nothing`() = runTest {
        // Arrange
        seedConversationWithMessages("Kept")
        val vm = ChatHistoryViewModel(repository)
        backgroundScope.launch { vm.uiState.collect {} }

        // Act
        vm.deleteConversation(999L)

        // Assert
        assertEquals(1, conversationIds().size)
        assertNull(vm.uiState.first { !it.isLoading }.recentlyDeleted)
    }

    @Test
    fun `deleteAllConversations removes every conversation and message`() = runTest {
        // Arrange
        val first = seedConversationWithMessages("First")
        val second = seedConversationWithMessages("Second")
        val vm = ChatHistoryViewModel(repository)

        // Act
        vm.deleteAllConversations()

        // Assert
        assertTrue(conversationIds().isEmpty())
        assertTrue(repository.getMessages(first).first().isEmpty())
        assertTrue(repository.getMessages(second).first().isEmpty())
    }

    @Test
    fun `openConversation marks only the selected conversation as active`() = runTest {
        // Arrange
        val first = seedConversationWithMessages("First")
        val second = seedConversationWithMessages("Second")
        val vm = ChatHistoryViewModel(repository)
        var openedId: Long? = null

        // Act
        vm.openConversation(first) { openedId = it }

        // Assert
        assertEquals(first, openedId)
        assertEquals(first, repository.getActiveConversation()?.id)
        assertFalse(repository.getConversationById(second)!!.isActive)
    }

    @Test
    fun `endActiveSession leaves no active conversation`() = runTest {
        // Arrange
        seedConversationWithMessages("Open")
        val vm = ChatHistoryViewModel(repository)

        // Act
        vm.endActiveSession()

        // Assert
        assertNull(repository.getActiveConversation())
    }
}
