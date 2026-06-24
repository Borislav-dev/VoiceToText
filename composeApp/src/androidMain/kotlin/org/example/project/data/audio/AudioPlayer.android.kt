package org.example.project.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.audio.IAudioPlayer

actual class AudioPlayer actual constructor(context: Any?) : IAudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    override val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    override val duration: StateFlow<Int> = _duration.asStateFlow()

    private var isPrepared = false

    override fun play(url: String) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setOnPreparedListener { mp ->
                    isPrepared = true
                    _duration.value = mp.duration
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracking()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = _duration.value
                    stopProgressTracking()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    stopProgressTracking()
                    true
                }
                try {
                    setDataSource(url)
                    prepareAsync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            if (isPrepared) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracking()
            }
        }
    }

    override fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            _isPlaying.value = false
            stopProgressTracking()
        }
    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        isPrepared = false
        _isPlaying.value = false
        _currentPosition.value = 0
        stopProgressTracking()
    }

    override fun seekTo(positionMs: Int) {
        if (isPrepared) {
            mediaPlayer?.seekTo(positionMs)
            _currentPosition.value = positionMs
        }
    }

    override fun release() {
        stopProgressTracking()
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
        _isPlaying.value = false
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPosition.value = mp.currentPosition
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }
}
