package org.example.project.domain.repository

interface ITranscriptionRepository {
    suspend fun transcribeAudio(filePath: String, contextPrompt: String? = null): Result<String>
    suspend fun analyzeText(instruction: String, text: String): Result<String>
    suspend fun cleanupTranscript(rawText: String, trackedKeywords: String? = null): Result<String>
}
