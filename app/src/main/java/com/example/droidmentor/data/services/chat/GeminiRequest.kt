package com.example.droidmentor.data.services.chat

import kotlinx.serialization.Serializable

/**
 * Represents the root request payload sent to the Gemini API's generateContent endpoint.
 *
 * @property contents A list of [Content] objects representing the conversation history or current prompt.
 */
@Serializable
data class GeminiRequest(
    val contents: List<Content>     // List of all interactions (history)
)

/**
 * Represents a single interaction turn within a conversation with the Gemini model.
 *
 * @property role Identifies the sender for this turn (e.g., "user" or "model"). Can be null for simple single-turn prompts.
 * @property parts The collection of content segments ([Part]) that form this interaction turn.
 */
@Serializable
data class Content(
    val role: String? = null,       // Who sent the message? (user or model)
    val parts: List<Part>
)

/**
 * Represents an individual piece of content within a conversation turn.
 * Currently configured for text messages, but extensible for multimodal input (images, audio, etc.).
 *
 * @property text The raw text content of this message part.
 */
@Serializable
data class Part(                    // The message content (It can be text, image, audio, etc...)
    val text: String = ""
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)