package org.example.project.domain.usecase

import org.example.project.domain.model.Note
import org.example.project.domain.repository.INotesRepository

class GetNoteUseCase(private val notesRepository: INotesRepository) {
    suspend operator fun invoke(noteId: String): Result<Note> = runCatching {
        notesRepository.getNoteById(noteId) ?: throw NoSuchElementException("Note not found")
    }
}
