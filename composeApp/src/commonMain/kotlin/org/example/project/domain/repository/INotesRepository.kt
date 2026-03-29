package org.example.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.Note

interface INotesRepository {
    suspend fun saveNote(note: Note)
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun getNoteById(id: String): Note?
    suspend fun deleteNote(id: String)
    fun getNotes(): Flow<List<Note>>
    suspend fun syncNotes()
    suspend fun uploadAudio(filePath: String, noteId: String): Result<String>
}
