package org.example.project.domain.usecase

import org.example.project.domain.repository.INotesRepository

class DeleteNoteUseCase(private val repository: INotesRepository) {
    suspend operator fun invoke(noteId: String): Result<Unit> = runCatching {
        repository.deleteNote(noteId)
    }
}
