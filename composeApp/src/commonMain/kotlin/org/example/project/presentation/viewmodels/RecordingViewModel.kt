package org.example.project.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import org.example.project.data.io.formatTimestamp
import org.example.project.domain.audio.IAudioRecorder
import org.example.project.domain.model.Note
import org.example.project.domain.repository.INotesRepository
import org.example.project.domain.repository.IProfileRepository
import org.example.project.domain.repository.ISubscriptionRepository
import org.example.project.domain.repository.ITranscriptionRepository

/**
 * MVI state for the recording screen.
 */
sealed interface RecordingState {
    data object Idle : RecordingState
    data object Recording : RecordingState
    data object Transcribing : RecordingState
    data class Success(val noteId: String) : RecordingState
    data class Error(val message: String) : RecordingState
}

class RecordingViewModel(
    private val audioRecorder: IAudioRecorder,
    private val transcriptionRepository: ITranscriptionRepository,
    private val notesRepository: INotesRepository,
    private val profileRepository: IProfileRepository,
    private val subscriptionRepository: ISubscriptionRepository
) : ViewModel() {

    companion object {
        private const val FREE_TIER_LIMIT_SECONDS = 300 // 5 minutes
        private const val WARNING_THRESHOLD_SECONDS = 270 // 4:30 = last 30s warning
        private const val MAX_FREE_RECORDS = 3
    }

    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    private val _trackedKeywords = MutableStateFlow("")
    val trackedKeywords: StateFlow<String> = _trackedKeywords.asStateFlow()

    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _freeRecordsLeft = MutableStateFlow(MAX_FREE_RECORDS)
    val freeRecordsLeft: StateFlow<Int> = _freeRecordsLeft.asStateFlow()

    private val _showTimeLimitDialog = MutableStateFlow(false)
    val showTimeLimitDialog: StateFlow<Boolean> = _showTimeLimitDialog.asStateFlow()

    private val _showPaywallDialog = MutableStateFlow(false)
    val showPaywallDialog: StateFlow<Boolean> = _showPaywallDialog.asStateFlow()

    private var timerJob: Job? = null
    private var pendingTitle: String = ""

    init {
        loadSubscription()
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            subscriptionRepository.getSubscription()
                .onSuccess { sub ->
                    _isPremium.value = sub.isPremium
                    _freeRecordsLeft.value = sub.freeRecordsLeft
                    println("Subscription loaded: premium=${sub.isPremium}, freeRecordsLeft=${sub.freeRecordsLeft}")
                }
                .onFailure { e ->
                    println("Failed to load subscription: ${e.message}")
                    // Keep defaults: not premium, 3 free records
                }
        }
    }

    fun updateTrackedKeywords(keywords: String) {
        _trackedKeywords.value = keywords
    }

    fun formatSeconds(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val mm = if (minutes < 10) "0$minutes" else "$minutes"
        val ss = if (seconds < 10) "0$seconds" else "$seconds"
        return "$mm:$ss"
    }

    fun startRecording() {
        // Check free tier record limit before starting
        if (!_isPremium.value && _freeRecordsLeft.value <= 0) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            val fileName = "recording_${Clock.System.now().toEpochMilliseconds()}"
            audioRecorder.startRecording(fileName)
            _recordingSeconds.value = 0
            _state.value = RecordingState.Recording

            // Start timer coroutine
            timerJob = viewModelScope.launch {
                while (_state.value is RecordingState.Recording) {
                    delay(1000L)
                    _recordingSeconds.value += 1

                    // Check free tier time limit
                    if (!_isPremium.value && _recordingSeconds.value >= FREE_TIER_LIMIT_SECONDS) {
                        _showTimeLimitDialog.value = true
                        stopAndProcessRecording(pendingTitle)
                        break
                    }
                }
            }
        }
    }

    fun stopAndProcessRecording(title: String) {
        pendingTitle = title
        timerJob?.cancel()
        timerJob = null

        viewModelScope.launch {
            try {
                // 1. Stop the recorder and get the file path
                val filePath = audioRecorder.stopRecording()
                val finalDurationSeconds = _recordingSeconds.value
                val finalDurationFormatted = formatSeconds(finalDurationSeconds)
                _state.value = RecordingState.Transcribing

                // 2. Validate file before sending
                val fileBytes = withContext(Dispatchers.Default) {
                    org.example.project.data.io.readFileBytes(filePath)
                }
                if (fileBytes.isEmpty()) {
                    throw IllegalStateException("Recording file is empty at: $filePath")
                }

                // 3. Send to Deepgram for transcription
                val transcriptionResult = transcriptionRepository.transcribeAudio(filePath)
                var transcribedText = transcriptionResult.getOrThrow()

                // 3.5 AI Meeting Assistant pipeline
                if (transcribedText.isNotBlank() && transcribedText != "No speech detected.") {
                    val keywords = _trackedKeywords.value.ifBlank { null }
                    val cleanupResult = transcriptionRepository.cleanupTranscript(transcribedText, keywords)
                    cleanupResult.onSuccess { transcribedText = it }
                }

                // 3.8 Upload Audio
                val now = Clock.System.now().toEpochMilliseconds()
                val noteId = "note_$now"
                
                var audioUrl: String? = null
                notesRepository.uploadAudio(filePath, noteId)
                    .onSuccess { url ->
                        audioUrl = url
                        println("Audio uploaded successfully: $url")
                    }
                    .onFailure { e ->
                        // Proceed with null audioUrl to prevent transcript data loss
                        println("Failed to upload audio: ${e.message}")
                    }

                // 4. Save the note to Supabase
                val noteTitle = title.ifBlank {
                    "Recording from ${formatTimestamp(now)}"
                }
                val note = Note(
                    id = noteId,
                    title = noteTitle,
                    content = transcribedText,
                    createdAt = now,
                    durationSeconds = finalDurationSeconds,
                    durationFormatted = finalDurationFormatted,
                    audioUrl = audioUrl
                )
                notesRepository.saveNote(note)

                // 5. Increment recordings used counter
                profileRepository.incrementRecordingsUsed()

                // 6. Decrement free record count (if not premium)
                if (!_isPremium.value) {
                    subscriptionRepository.decrementFreeRecord()
                        .onSuccess {
                            _freeRecordsLeft.value = (_freeRecordsLeft.value - 1).coerceAtLeast(0)
                            println("Free record used. Remaining: ${_freeRecordsLeft.value}")
                        }
                        .onFailure { e ->
                            println("Failed to decrement free record: ${e.message}")
                        }
                }

                // 7. Done!
                _state.value = RecordingState.Success(noteId)
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = RecordingState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun dismissTimeLimitDialog() {
        _showTimeLimitDialog.value = false
    }

    fun dismissPaywallDialog() {
        _showPaywallDialog.value = false
    }

    fun resetState() {
        _state.value = RecordingState.Idle
        _recordingSeconds.value = 0
    }
}
