package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.project.BuildKonfig
import org.example.project.data.io.readFileBytes
import org.example.project.domain.repository.ITranscriptionRepository


@Serializable
data class DeepgramResponse(
    val results: DeepgramResults
)

@Serializable
data class DeepgramResults(
    val channels: List<DeepgramChannel>
)

@Serializable
data class DeepgramChannel(
    val alternatives: List<DeepgramAlternative>
)

@Serializable
data class DeepgramAlternative(
    val transcript: String,
    val confidence: Double = 0.0
)

// ── Repository implementation ───────────────────────────────────────────────

class DeepgramTranscriptionRepositoryImpl(
    private val httpClient: HttpClient
) : ITranscriptionRepository {

    companion object {
        private const val DEEPGRAM_BASE_URL =
            "https://api.deepgram.com/v1/listen?model=nova-3&smart_format=true&detect_language=true&paragraphs=true&diarize=true"
        private const val CHAT_URL = "https://api.openai.com/v1/chat/completions"

        private val DEFAULT_KEYWORDS = listOf("Supabase", "Kotlin", "Jetpack Compose", "API")
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildDeepgramUrl(contextPrompt: String?): String {
        val allKeywords = DEFAULT_KEYWORDS.toMutableList()

        if (!contextPrompt.isNullOrBlank()) {
            contextPrompt.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { allKeywords.add(it) }
        }

        val keywordParams = allKeywords.joinToString("") { keyword ->
            "&keyterm=${keyword.replace(" ", "+")}"
        }

        return "$DEEPGRAM_BASE_URL$keywordParams"
    }

    override suspend fun transcribeAudio(filePath: String, contextPrompt: String?): Result<String> {
        val fileBytes = readFileBytes(filePath)
        val url = buildDeepgramUrl(contextPrompt)

        val fileSizeMb = fileBytes.size / (1024.0 * 1024.0)// Manual formatting for KMP commonMain
        val formattedSize = (fileSizeMb * 100).toInt() / 100.0
        println("🎙️ DIAGNOSTICS: Audio file size: $formattedSize MB")

        val response = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Token ${BuildKonfig.DEEPGRAM_API_KEY.replace("\"", "")}")
            header(HttpHeaders.ContentType, ContentType.parse("audio/mp4"))
            setBody(fileBytes)
        }

        val rawResponse = response.bodyAsText()

        if (!response.status.isSuccess()) {
            println("Deepgram API Error: $rawResponse")
            return Result.failure(Exception("Deepgram Error: $rawResponse"))
        }

        return runCatching {
            val deepgramResponse = json.decodeFromString<DeepgramResponse>(rawResponse)

            val transcript = deepgramResponse.results.channels
                .firstOrNull()?.alternatives?.firstOrNull()?.transcript

            transcript?.ifBlank { "No speech detected." } ?: "No speech detected."
        }
    }

    override suspend fun analyzeText(instruction: String, text: String): Result<String> = runCatching {
        val requestBody = JsonObject(mapOf(
            "model" to JsonPrimitive("gpt-4o-mini"),
            "temperature" to JsonPrimitive(0.2),
            "messages" to JsonArray(listOf(
                JsonObject(mapOf(
                    "role" to JsonPrimitive("system"),
                    "content" to JsonPrimitive(instruction)
                )),
                JsonObject(mapOf(
                    "role" to JsonPrimitive("user"),
                    "content" to JsonPrimitive(text)
                ))
            ))
        ))

        val response = httpClient.post(CHAT_URL) {
            bearerAuth(BuildKonfig.OPENAI_API_KEY)
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(requestBody.toString())
        }

        val responseBody = response.bodyAsText()
        val jsonElement = Json.parseToJsonElement(responseBody)
        jsonElement.jsonObject["choices"]
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content
            ?: throw IllegalStateException("No AI response in: $responseBody")
    }

    override suspend fun cleanupTranscript(rawText: String, trackedKeywords: String?): Result<String> {
        val keywordsSection = if (!trackedKeywords.isNullOrBlank()) {
            "\n\nTracked Keywords: $trackedKeywords"
        } else {
            ""
        }

        val instruction = """You are an expert AI Meeting Assistant. I will provide a raw voice transcript and optionally a list of 'Tracked Keywords'. Your job is to output a well-formatted Markdown response with EXACTLY these 2 sections:

### 🎯 Tracked Mentions
[If tracked keywords were provided, briefly summarize what was said about them. If none were provided or found, say 'No specific tracked items found.']

### ✅ Action Items
[Extract any implied or explicit tasks, decisions, or to-dos from the text as a bulleted list. If none, say 'No action items detected.']

CRITICAL: Keep the output in the EXACT SAME LANGUAGE as the original transcript. DO NOT output, summarize, or rewrite the original full transcript."""

        val userMessage = rawText + keywordsSection
        return analyzeText(instruction, userMessage).map { aiSummary ->
            "$aiSummary\n\n---\n### 📝 Original Transcript\n$rawText"
        }
    }
}