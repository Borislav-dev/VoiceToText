package org.example.project.domain.usecase

import org.example.project.domain.model.Note
import org.example.project.domain.repository.INotesRepository

class UpdateNoteUseCase(private val repository: INotesRepository) {
    suspend operator fun invoke(note: Note): Result<Unit> = repository.updateNote(note)
}
