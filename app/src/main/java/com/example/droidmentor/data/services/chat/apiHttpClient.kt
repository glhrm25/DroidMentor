package com.example.droidmentor.data.services.chat

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


/** Host of the Gemini API. */
internal const val API_HOST = "generativelanguage.googleapis.com"

/** Path of the Gemini model used to generate the responses. */
internal const val API_MODEL = "v1beta/models/gemini-3.6-flash"

/** Endpoint and query that make the API answer as a stream of server-sent events (SSE). */
internal const val CONTENT_GENERATION_MODE = "streamGenerateContent?alt=sse"

/** Full URL of the streaming content generation request. */
internal const val API_URL = "https://${API_HOST}/${API_MODEL}:${CONTENT_GENERATION_MODE}"

/**
 * The JSON configuration used to serialize requests and parse responses. Fields not declared
 * in the DTOs are ignored, and lenient parsing tolerates slightly malformed JSON.
 */
internal val geminiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/** How long establishing a connection may take before it is considered failed. */
internal const val CONNECT_TIMEOUT_MILLIS = 10_000L

/**
 * The maximum time without receiving data before the request is considered failed. It measures
 * the gap between data, not the total duration, so long streamed responses are not cut.
 */
internal const val SOCKET_TIMEOUT_MILLIS = 30_000L

/**
 * Creates the HTTP client used to access the Gemini API. It is meant to be shared by the whole
 * app, so that connections are reused.
 *
 * @param engine The HTTP engine (e.g. OkHttp in production, MockEngine in tests).
 */
fun createApiHttpClient(
    engine: HttpClientEngine,
): HttpClient = HttpClient(engine) {
    // Fails requests that take too long to connect or that stop receiving data
    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
    }

    // Serializes request bodies to JSON and parses the responses
    install(ContentNegotiation) {
        json(geminiJson)
    }

    // Enables server-sent events, used to receive the response in chunks. Automatic reconnection
    // is disabled: it would resend the POST and could duplicate the response
    install(SSE){
        maxReconnectionAttempts = 0
    }
}