package org.example.project.data.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.example.project.domain.audio.IAudioPlayer

actual class AudioPlayer actual constructor(context: Any?) : IAudioPlayer {
    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPosition = MutableStateFlow(0)
    override val currentPosition: StateFlow<Int> = _currentPosition

    private val _duration = MutableStateFlow(0)
    override val duration: StateFlow<Int> = _duration

    override fun play(url: String) {
        // Stub for iOS
    }

    override fun pause() {
        // Stub for iOS
    }

    override fun stop() {
        // Stub for iOS
    }

    override fun seekTo(positionMs: Int) {
        // Stub for iOS
    }

    override fun release() {
        // Stub for iOS
    }
}
