package org.example.project.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import org.example.project.data.io.readFileBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.project.domain.model.Note
import org.example.project.domain.repository.INotesRepository

class SupabaseNotesRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : INotesRepository {

    companion object {
        private const val TABLE_NAME = "notes"
    }

    // Single Source of Truth
    // Reverted back to instance-level since Koin now guarantees this repository is a global singleton
    private val _notesFlow = MutableStateFlow<List<Note>>(emptyList())

    override suspend fun saveNote(note: Note) {
        supabaseClient.from(TABLE_NAME).insert(note)
        // Update SSOT instantly: add new note at the top
        _notesFlow.value = listOf(note) + _notesFlow.value
    }

    override suspend fun updateNote(note: Note): Result<Unit> = runCatching {
        supabaseClient.from(TABLE_NAME).update(note) {
            filter { eq("id", note.id) }
        }

        // Update SSOT
        val currentNotes = _notesFlow.value.toMutableList()
        val index = currentNotes.indexOfFirst { it.id == note.id }
        if (index != -1) {
            currentNotes[index] = note
            _notesFlow.value = currentNotes
        }
    }

    override suspend fun getNoteById(id: String): Note? {
        return supabaseClient.from(TABLE_NAME)
            .select {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<Note>()
    }

    override suspend fun deleteNote(id: String) {
        // Optimistically remove from SSOT instantly
        _notesFlow.value = _notesFlow.value.filter { it.id != id }

        supabaseClient.from(TABLE_NAME).delete {
            filter { eq("id", id) }
        }
    }

    override fun getNotes(): Flow<List<Note>> {
        // Return the continuous stream
        return _notesFlow.asStateFlow()
    }

    override suspend fun syncNotes() {
        // Fetch fresh data from Supabase
        val notes = supabaseClient.from(TABLE_NAME)
            .select {
                order("createdAt", Order.DESCENDING)
            }
            .decodeList<Note>()

        // Update SSOT
        _notesFlow.value = notes
    }

    override suspend fun uploadAudio(filePath: String, noteId: String): Result<String> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated")

        val fileBytes = readFileBytes(filePath)
        val path = "$userId/$noteId.m4a"

        val bucket = supabaseClient.storage["audio_uploads"]
        bucket.upload(path, fileBytes)

        bucket.publicUrl(path)
    }
}
