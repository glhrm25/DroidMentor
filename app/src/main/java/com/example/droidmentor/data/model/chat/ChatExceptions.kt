package com.example.droidmentor.data.model.chat

sealed class ChatException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

class NoConnectivityException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)

class RateLimitExceededException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)

class UnexpectedChatException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)

class ParsingException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)

class BlockedRequestException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)

class ResponseTruncatedException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)

class RequestTimeoutException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)

class MissingApiKeyException(message: String? = null, cause: Throwable? = null) :
    ChatException(message, cause)