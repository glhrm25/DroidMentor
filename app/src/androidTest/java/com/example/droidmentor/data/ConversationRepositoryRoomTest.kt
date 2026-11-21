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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies conversation removal and session persistence against a real (in-memory) Room database.
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
