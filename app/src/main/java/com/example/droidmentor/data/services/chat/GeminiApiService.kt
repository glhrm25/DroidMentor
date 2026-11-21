package com.example.droidmentor.data.services.chat

import com.example.droidmentor.R
import com.example.droidmentor.data.model.chat.BlockedRequestException
import com.example.droidmentor.data.model.chat.ChatException
import com.example.droidmentor.data.model.chat.MissingApiKeyException
import com.example.droidmentor.data.model.chat.NoConnectivityException
import com.example.droidmentor.data.model.chat.ParsingException
import com.example.droidmentor.data.model.chat.RequestTimeoutException
import com.example.droidmentor.data.model.chat.ResponseTruncatedException
import com.example.droidmentor.data.model.chat.UnexpectedChatException
import com.example.droidmentor.preferences.PreferencesRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.sse.SSEClientException
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.util.concurrent.CancellationException


class GeminiApiService(
    private val client: HttpClient,
    private val preferencesRepository: PreferencesRepository
): ChatAPIService {
    override fun generateContentStream(history: List<Content>): Flow<String> =
        flow {
            val apiKey = preferencesRepository.apiKey
                .first()
                ?.takeIf { it.isNotBlank() }
                ?: throw MissingApiKeyException(
                    message = R.string.active_chat_missing_key_message.toString()
                )

            val requestBody = GeminiRequest(contents = history)

            client.sse(
                urlString = API_URL,
                request = {
                    method = HttpMethod.Post
                    setBody(requestBody)
                    header("x-goog-api-key", apiKey)
                    contentType(ContentType.Application.Json)
                }
            ) {
                // incoming events flow
                incoming.collect { event ->
                    val jsonString = event.data
                    if (!jsonString.isNullOrBlank()) {
                        val response = try {
                            geminiJson.decodeFromString<GeminiResponse>(jsonString)
                        } catch (e: SerializationException) {
                            throw ParsingException(e.message, e)
                        }

                        response.promptFeedback?.blockReason?.let { blockReason ->
                            throw BlockedRequestException(message = blockReason)
                        }

                        val candidate = response.candidates?.firstOrNull()
                        val text = candidate?.content?.parts?.joinToString("") { it.text }

                        if (!text.isNullOrEmpty()) {        // Events with no text (e.g. only metadata or finishReason) are skipped
                            emit(text)
                        }

                        candidate?.finishReason
                            ?.takeIf { it != "STOP" }       // "Stop" means the request stopped naturally
                            ?.let {
                                throw if (it == "MAX_TOKENS") ResponseTruncatedException(message = it)
                                else BlockedRequestException(message = it)
                            }
                    }
                }
            }
        }.catch { e ->
            throw when(e) {
                is ChatException, is CancellationException -> e
                is SSEClientException -> TODO("CHECK HTTP STATUS CODE ON e.response")
                is HttpRequestTimeoutException -> RequestTimeoutException(e.message, e)
                is IOException -> NoConnectivityException(e.message, e)
                else -> UnexpectedChatException(e.message, e)
            }
        }
}
