package com.example.chalkitup.network

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.SerialName

object ChatBot {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }) // Allows ignoring unknown keys
        }
        engine {
            requestTimeout = 60_000 // ⏳ 60 seconds timeout
        }
    }

    private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"

    suspend fun sendMessage(chatHistory: List<Message>): String {
        if (API_KEY.isBlank()) {
            return "Error: API Key is missing. Please set it inside ChatBot.kt."
        }

        val requestBody = ChatRequest(
            model = "openai/gpt-4o", // Update to free API once fixed
            messages = listOf(Message("system", "You are a helpful tutor assistant.")) + chatHistory, // ✅ Include full history
            max_tokens = 2500
        )

        println("📩 Request body: " + Json.encodeToString(requestBody))

        return try {
            val response: HttpResponse = client.post(API_URL) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $API_KEY")
                    append(HttpHeaders.ContentType, ContentType.Application.Json)
                }
                setBody(Json.encodeToString(requestBody))
            }

            val responseText = response.bodyAsText()
            println("🔍 DEBUG - Response: $responseText")

            if (response.status.value != 200) {
                return "Error: $responseText"
            }

            val parsedResponse = Json.decodeFromString<ChatResponse>(responseText)

            return parsedResponse.choices.getOrNull(0)?.message?.content ?: "Error: No valid response from AI"

        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}

@Serializable
data class ChatRequest(val model: String, val messages: List<Message>, val max_tokens: Int)

@Serializable
data class ChatResponse(
    val id: String?,
    val provider: String?,
    val model: String?,
    @SerialName("object") val objectType: String?,
    val created: Long?,
    val choices: List<Choice>,
    val system_fingerprint: String?,
    val usage: Usage?
)

@Serializable
data class Usage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?,
    val prompt_tokens_details: TokenDetails?,
    val completion_tokens_details: TokenDetails?
)

@Serializable
data class TokenDetails(
    val cached_tokens: Int? = null,
    val reasoning_tokens: Int? = null
)

@Serializable
data class Choice(
    val logprobs: String?,
    val finish_reason: String?,
    val native_finish_reason: String?,
    val index: Int,
    val message: Message
)

@Serializable
data class Message(
    val role: String,
    val content: String,
    val refusal: String? = null
)
