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
import org.example.project.domain.usecase.*

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
    private val searchInTextUseCase: SearchInTextUseCase,
    private val audioPlayer: IAudioPlayer,
    private val shareManager: IShareManager
) : ViewModel() {

    private val _state = MutableStateFlow<NoteDetailsState>(NoteDetailsState.Loading)
    val state: StateFlow<NoteDetailsState> = _state.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Persistent Target Language for all AI actions
    private val _currentTargetLanguage = MutableStateFlow<String?>(null)
    val currentTargetLanguage: StateFlow<String?> = _currentTargetLanguage.asStateFlow()

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

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<IntRange>>(emptyList())
    val searchResults: StateFlow<List<IntRange>> = _searchResults.asStateFlow()

    private val _currentResultIndex = MutableStateFlow(-1)
    val currentResultIndex: StateFlow<Int> = _currentResultIndex.asStateFlow()

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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val currentState = _state.value
        if (currentState is NoteDetailsState.Success && query.isNotBlank()) {
            val matches = searchInTextUseCase(currentState.note.content, query)
            _searchResults.value = matches
            _currentResultIndex.value = if (matches.isNotEmpty()) 0 else -1
        } else {
            _searchResults.value = emptyList()
            _currentResultIndex.value = -1
        }
    }

    fun goToNextSearchResult() {
        val results = _searchResults.value
        if (results.isNotEmpty()) {
            _currentResultIndex.value = (_currentResultIndex.value + 1) % results.size
        }
    }

    fun goToPreviousSearchResult() {
        val results = _searchResults.value
        if (results.isNotEmpty()) {
            val nextIndex = _currentResultIndex.value - 1
            _currentResultIndex.value = if (nextIndex < 0) results.size - 1 else nextIndex
        }
    }

    fun performAiAction(type: String, text: String, targetLanguage: String? = null) {
        // If it's a translation, update the persistent target language
        if (type == "Translate" && targetLanguage != null) {
            _currentTargetLanguage.value = targetLanguage
        }

        val effectiveLanguage = targetLanguage ?: _currentTargetLanguage.value

        _isAiLoading.value = true
        _aiResponse.value = "AI is processing..."
        
        val langRule = if (effectiveLanguage != null) {
            "You MUST format your output and generate ALL text (headers and content) EXCLUSIVELY in $effectiveLanguage. Under NO circumstances should you use any other language."
        } else {
            "You MUST generate your response EXCLUSIVELY in the exact same language as the input text. Under NO circumstances should you translate it."
        }

        val instruction = when (type) {
            "Summary" -> "You are a professional assistant. Summarize the following text concisely. $langRule"
            "Action Items" -> "You are a professional assistant. Extract action items from the following text as a bulleted list. $langRule"
            "To Email" -> "You are a professional assistant. Format the following text as a professional email. $langRule"
            "Translate" -> """
                You are a professional translator expert in technical and conversational context. 
                Translate the ENTIRE provided text into $effectiveLanguage.
                
                CRITICAL REQUIREMENTS:
                1. TRANSLATE ALL HEADERS: You MUST translate section headers like '### 🎯 Tracked Mentions', '### ✅ Action Items', and '### 📝 Original Transcript' into their natural and correct equivalents in $effectiveLanguage.
                2. CONTEXTUAL ACCURACY: Ensure the context and specific meaning within each section are preserved and accurately translated.
                3. PRESERVE FORMATTING: Keep all Markdown formatting (e.g., #, ##, **, -, 1.), newlines, and emojis exactly as they are in the source.
                4. COMPLETE TRANSLATION: Do not omit any part of the text. Translate every word.
                5. NO META-TALK: Provide ONLY the translated text.
            """.trimIndent()
            else -> "You are a professional assistant. Analyze the following text. $langRule"
        }

        viewModelScope.launch {
            analyzeTextUseCase(instruction, text).onSuccess { responseText ->
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
