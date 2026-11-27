package com.example.droidmentor.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.droidmentor.data.local.AppDatabase
import com.example.droidmentor.data.repository.RoomConversationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies conversation and message removal and session persistence against a real (in-memory) Room database.
 */
@RunWith(AndroidJUnit4::class)
class ConversationRepositoryRoomTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: RoomConversationRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomConversationRepository(database.conversationDao(), database.messageDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun deleteConversation_whenItHasMessages_removesMessagesByCascade() = runTest {
        // Arrange
        val id = repository.createConversation("To delete")
        repository.addMessage(id, "USER", "Hello")
        repository.addMessage(id, "MODEL", "Hi")

        // Act
        val snapshot = repository.deleteConversation(id)

        // Assert
        assertNotNull(snapshot)
        assertEquals(2, snapshot!!.messages.size)
        assertNull(repository.getConversationById(id))
        assertTrue(database.messageDao().getMessagesForConversationSync(id).isEmpty())
    }

    @Test
    fun restoreConversation_afterDelete_bringsBackConversationAndMessages() = runTest {
        // Arrange
        val id = repository.createConversation("Restorable")
        repository.addMessage(id, "USER", "Hello")
        val snapshot = repository.deleteConversation(id)!!

        // Act
        repository.restoreConversation(snapshot)

        // Assert
        assertEquals("Restorable", repository.getConversationById(id)?.title)
        assertEquals(
            listOf("Hello"),
            database.messageDao().getMessagesForConversationSync(id).map { it.text }
        )
    }

    @Test
    fun deleteAllConversations_whenSeveralExist_leavesHistoryEmpty() = runTest {
        // Arrange
        repository.addMessage(repository.createConversation("A"), "USER", "a")
        repository.addMessage(repository.createConversation("B"), "USER", "b")

        // Act
        repository.deleteAllConversations()

        // Assert
        assertTrue(repository.allConversations.first().isEmpty())
    }

    @Test
    fun updateMessage_whenItExists_replacesTheTextKeepingTheOrder() = runTest {
        // Arrange
        val id = repository.createConversation("Chat")
        val question = repository.addMessage(id, "USER", "Original question")
        repository.addMessage(id, "MODEL", "Answer")

        // Act
        val updated = repository.updateMessage(question, "Rewritten question")

        // Assert
        assertTrue(updated)
        assertEquals(
            listOf("Rewritten question", "Answer"),
            repository.getMessages(id).first().map { it.text }
        )
    }

    @Test
    fun updateMessage_whenItDoesNotExist_returnsFalse() = runTest {
        // Arrange
        val id = repository.createConversation("Chat")
        repository.addMessage(id, "USER", "Question")

        // Act
        val updated = repository.updateMessage(messageId = 999, text = "Rewritten")

        // Assert
        assertFalse(updated)
        assertEquals(listOf("Question"), repository.getMessages(id).first().map { it.text })
    }

    @Test
    fun deleteMessage_whenItExists_removesOnlyThatMessage() = runTest {
        // Arrange
        val id = repository.createConversation("Chat")
        repository.addMessage(id, "USER", "Question")
        val reply = repository.addMessage(id, "MODEL", "Partial answer")

        // Act
        val deleted = repository.deleteMessage(reply)

        // Assert
        assertTrue(deleted)
        assertEquals(listOf("Question"), repository.getMessages(id).first().map { it.text })
    }

    @Test
    fun deleteMessage_whenItDoesNotExist_returnsFalse() = runTest {
        // Arrange
        val id = repository.createConversation("Chat")
        repository.addMessage(id, "USER", "Question")

        // Act
        val deleted = repository.deleteMessage(messageId = 999)

        // Assert
        assertFalse(deleted)
        assertEquals(1, repository.getMessages(id).first().size)
    }

    @Test
    fun deleteMessagesFrom_whenMessageIsInTheMiddle_removesItAndTheFollowingOnes() = runTest {
        // Arrange
        val id = repository.createConversation("Chat")
        val otherId = repository.createConversation("Other chat")
        repository.addMessage(id, "USER", "First question")
        repository.addMessage(id, "MODEL", "First answer")
        val rewritten = repository.addMessage(id, "USER", "Second question")
        repository.addMessage(otherId, "USER", "Unrelated")
        repository.addMessage(id, "MODEL", "Second answer")

        // Act
        val deleted = repository.deleteMessagesFrom(rewritten)

        // Assert
        assertEquals(2, deleted)
        assertEquals(
            listOf("First question", "First answer"),
            repository.getMessages(id).first().map { it.text }
        )
        assertEquals(listOf("Unrelated"), repository.getMessages(otherId).first().map { it.text })
    }

    @Test
    fun deleteMessagesFrom_whenMessageDoesNotExist_deletesNothing() = runTest {
        // Arrange
        val id = repository.createConversation("Chat")
        repository.addMessage(id, "USER", "Question")

        // Act
        val deleted = repository.deleteMessagesFrom(messageId = 999)

        // Assert
        assertEquals(0, deleted)
        assertEquals(1, repository.getMessages(id).first().size)
    }

    @Test
    fun createConversation_whenAnotherIsActive_keepsASingleActiveSession() = runTest {
        // Arrange
        repository.createConversation("Older")

        // Act
        val newest = repository.createConversation("Newest")

        // Assert
        assertEquals(newest, repository.getActiveConversation()?.id)
        assertEquals(1, repository.allConversations.first().count { it.isActive })
    }
}
