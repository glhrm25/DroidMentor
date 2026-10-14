package com.example.droidmentor.active_chat

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


/**
 * Service class responsible for handling HTTP network communication with the Google Gemini API using Ktor.
 * Encapsulates client configuration, JSON serialization, request logging, and API authentication.
 *
 * @property apiKey The Google Gemini API key used to authenticate requests.
 */
class GeminiApiService(
    private val apiKey: String
) {
    /**
     * Pre-configured Ktor [HttpClient] instance using the Android engine.
     * Includes JSON content negotiation (ignoring unknown keys) and HTTP request/response body logging.
     */
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true        // Prevents crashes if Gemini API introduces new unmapped fields
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.BODY               // Logs full HTTP requests and responses in Logcat for debugging
        }
    }

    /**
     * Sends the complete conversation history to the Gemini API and returns the generated model response.
     * Wraps the network call in Kotlin's [Result] to safely handle API or connectivity failures without crashing.
     *
     * @param history A list of [Content] objects representing the back-and-forth chat history.
     * @return A [Result] containing the mentor's generated text response on success, or an exception on failure.
     */
    suspend fun generateContent(history: List<Content>): Result<String> {
        return runCatching {
            val request = GeminiRequest(contents = history)

            val response: GeminiResponse = client.post(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent?key=$apiKey"
            ) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            // Extracts the text content from the first candidate in the response payload
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("The mentors answer is blank.")
        }
    }
}
