package com.example.droidmentor.data

import com.example.droidmentor.data.local.entity.ConversationEntity
import com.example.droidmentor.data.local.entity.MessageEntity
import com.example.droidmentor.data.model.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for domain mapping and entity properties in the data layer.
 */
class ConversationModelTest {

    @Test
    fun conversationEntity_toDomainMapping_isCorrect() {
        val now = 1700000000000L
        val entity = ConversationEntity(
            id = 42L,
            title = "Test Conversation",
            createdAt = now,
            updatedAt = now + 5000L,
            isActive = true
        )

        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("Test Conversation", domain.title)
        assertEquals(now, domain.createdAt)
        assertEquals(now + 5000L, domain.updatedAt)
        assertTrue(domain.isActive)
    }

    @Test
    fun messageEntity_defaultValues_areCorrect() {
        val message = MessageEntity(
            conversationId = 10L,
            role = "USER",
            text = "Hello Mentor"
        )

        assertEquals(0L, message.id)
        assertEquals(10L, message.conversationId)
        assertEquals("USER", message.role)
        assertEquals("Hello Mentor", message.text)
        assertNull(message.imageUri)
        assertTrue(message.timestamp > 0)
    }
}
