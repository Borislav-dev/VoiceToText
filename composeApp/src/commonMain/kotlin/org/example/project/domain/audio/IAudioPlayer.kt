package org.example.project.domain.audio

import kotlinx.coroutines.flow.StateFlow

interface IAudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentPosition: StateFlow<Int>
    val duration: StateFlow<Int>

    fun play(url: String)
    fun pause()
    fun stop()
    fun seekTo(positionMs: Int)
    fun release()
}
