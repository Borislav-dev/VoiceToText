package org.example.project.domain.usecase

import org.example.project.domain.repository.ITranscriptionRepository

class AnalyzeTextUseCase(private val repository: ITranscriptionRepository) {
    suspend operator fun invoke(instruction: String, text: String): Result<String> =
        repository.analyzeText(instruction, text)
}
