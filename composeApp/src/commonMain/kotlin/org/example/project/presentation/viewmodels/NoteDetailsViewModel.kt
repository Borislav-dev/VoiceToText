package org.example.project.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.audio.IAudioPlayer
import org.example.project.domain.model.Note
import org.example.project.domain.share.IShareManager
import org.example.project.domain.usecase.AnalyzeTextUseCase
import org.example.project.domain.usecase.DeleteNoteUseCase
import org.example.project.domain.usecase.GetNoteUseCase
import org.example.project.domain.usecase.UpdateNoteUseCase

sealed interface NoteDetailsState {
    data object Loading : NoteDetailsState
    data class Success(val note: Note) : NoteDetailsState
    data class Error(val message: String) : NoteDetailsState
}

class NoteDetailsViewModel(
    private val noteId: String,
    private val getNoteUseCase: GetNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val analyzeTextUseCase: AnalyzeTextUseCase,
    private val audioPlayer: IAudioPlayer,
    private val shareManager: IShareManager
) : ViewModel() {

    private val _state = MutableStateFlow<NoteDetailsState>(NoteDetailsState.Loading)
    val state: StateFlow<NoteDetailsState> = _state.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Audio Playback State
    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying
    val audioPosition: StateFlow<Int> = audioPlayer.currentPosition
    val audioDuration: StateFlow<Int> = audioPlayer.duration

    // Editing State
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editedTitle = MutableStateFlow("")
    val editedTitle: StateFlow<String> = _editedTitle.asStateFlow()

    private val _editedContent = MutableStateFlow("")
    val editedContent: StateFlow<String> = _editedContent.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            getNoteUseCase(noteId).onSuccess { note ->
                _state.value = NoteDetailsState.Success(note)
            }.onFailure { e ->
                _state.value = NoteDetailsState.Error(e.message ?: "Failed to load note")
            }
        }
    }

    fun performAiAction(type: String, text: String, targetLanguage: String? = null) {
        _isAiLoading.value = true
        _aiResponse.value = "AI is processing..."
        
        val langRule = if (targetLanguage != null) {
            "You MUST format your output and generate ALL text EXCLUSIVELY in $targetLanguage. Under NO circumstances should you use any other language."
        } else {
            "You MUST generate your response EXCLUSIVELY in the exact same language as the input text. Under NO circumstances should you translate it."
        }

        val instruction = when (type) {
            "Summary" -> "You are a professional assistant. Summarize the following text concisely. $langRule"
            "Action Items" -> "You are a professional assistant. Extract action items from the following text as a bulleted list. $langRule"
            "To Email" -> "You are a professional assistant. Format the following text as a professional email. $langRule"
            "Translate" -> "You are a professional translator. Translate the following text into ${targetLanguage}. Provide ONLY the translated text, without any conversational filler or markdown."
            else -> "You are a professional assistant. Analyze the following text. $langRule"
        }

        viewModelScope.launch {
            analyzeTextUseCase(instruction, text).onSuccess { responseText ->
                val currentState = _state.value
                if (currentState is NoteDetailsState.Success) {
                    val note = currentState.note
                    val updatedNote = if (type == "Translate" && targetLanguage != null) {
                        note.copy(content = responseText, language = targetLanguage)
                    } else note
                    
                    if (updatedNote != note) {
                        updateNoteUseCase(updatedNote)
                        _state.value = NoteDetailsState.Success(updatedNote)
                    }
                }
                _aiResponse.value = responseText
            }.onFailure {
                _aiResponse.value = "Failed to process Action. Please try again."
            }
            _isAiLoading.value = false
        }
    }

    fun deleteNote(onDeleted: () -> Unit) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId).onSuccess {
                onDeleted()
            }
        }
    }

    fun retry() {
        _state.value = NoteDetailsState.Loading
        loadNote()
    }

    fun playAudio(url: String) {
        audioPlayer.play(url)
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun seekAudio(positionMs: Float) {
        audioPlayer.seekTo(positionMs.toInt())
    }

    fun updateEditedTitle(title: String) {
        _editedTitle.value = title
    }

    fun updateEditedContent(content: String) {
        _editedContent.value = content
    }

    fun toggleEditMode() {
        val currentEditState = _isEditing.value
        if (!currentEditState) {
            val currentState = _state.value
            if (currentState is NoteDetailsState.Success) {
                _editedTitle.value = currentState.note.title
                _editedContent.value = currentState.note.content
            }
        }
        _isEditing.value = !currentEditState
    }

    fun saveChanges() {
        val currentState = _state.value
        if (currentState is NoteDetailsState.Success) {
            _isAiLoading.value = true
            val updatedNote = currentState.note.copy(
                title = _editedTitle.value,
                content = _editedContent.value
            )
            viewModelScope.launch {
                updateNoteUseCase(updatedNote).onSuccess {
                    _state.value = NoteDetailsState.Success(updatedNote)
                    _isEditing.value = false
                }.onFailure {
                    _isEditing.value = false
                }
                _isAiLoading.value = false
            }
        }
    }

    fun shareNote(note: Note) {
        shareManager.shareText(note.title, note.content)
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
