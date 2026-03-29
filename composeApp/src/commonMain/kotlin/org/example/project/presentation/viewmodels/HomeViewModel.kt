package org.example.project.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.example.project.domain.model.Note
import org.example.project.domain.repository.INotesRepository
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val notes: List<Note>) : HomeState
    data class Error(val message: String) : HomeState
}

class HomeViewModel(
    private val notesRepository: INotesRepository
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Optimization: Track the current fetch job to cancel it if a new one is requested.
    // This prevents multiple concurrent network requests and race conditions.
    private var fetchJob: Job? = null

    init {
        // 1. Start collecting the continuous SSOT stream
        viewModelScope.launch {
            notesRepository.getNotes()
                .distinctUntilChanged()
                .collect { notes ->
                    // Set loading false since we have data (even if empty)
                    if (_state.value is HomeState.Loading && notes.isEmpty()) {
                        // Let the UI briefly show loading until first sync finishes
                        // unless we explicitly know there are 0 notes from cache
                    } else {
                        _state.value = HomeState.Success(notes)
                    }
                }
        }
        // 2. Trigger initial fetch
        loadNotes()
    }

    /**
     * Loads notes from the repository via sync.
     */
    fun loadNotes() {
        if (_state.value !is HomeState.Success) {
            _state.value = HomeState.Loading
        }
        fetchNotesInternal()
    }

    /**
     * Triggers a user-initiated refresh.
     */
    fun refreshNotes() {
        _isRefreshing.value = true
        fetchNotesInternal()
    }

    private fun fetchNotesInternal() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            val startTime = Clock.System.now().toEpochMilliseconds()
            try {
                // Sync network data. This will update the StateFlow in the Repository,
                // which automatically triggers the collect block in init{}.
                notesRepository.syncNotes()
                val duration = Clock.System.now().toEpochMilliseconds() - startTime
                println("[Performance] Notes network sync finished in ${duration}ms.")
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    e.printStackTrace()
                    _state.value = HomeState.Error(e.message ?: "Failed to sync notes")
                }
            } finally {
                _isRefreshing.value = false
                
                // If it was still loading but sync failed (or completed with no notes), 
                // force success to hide spinner and show empty state
                if (_state.value is HomeState.Loading) {
                    _state.value = HomeState.Success(emptyList())
                }
            }
        }
    }

    fun deleteNoteById(id: String) {
        // The repository handles optimistic SSOT update now, so we just call delete.
        viewModelScope.launch {
            try {
                notesRepository.deleteNote(id)
            } catch (e: Exception) {
                e.printStackTrace()
                // Revert optimistic update by reloading data on failure
                notesRepository.syncNotes()
            }
        }
    }
}