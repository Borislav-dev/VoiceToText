package org.example.project.data.audio

import org.example.project.domain.audio.IAudioRecorder

/**
 * Platform-specific audio recorder.
 * On Android: uses MediaRecorder to capture audio as .m4a.
 * On iOS: stub (not yet implemented).
 *
 * @param context Platform-specific context (Android Context as Any?)
 */
expect class AudioRecorder(context: Any?) : IAudioRecorder {
    override suspend fun startRecording(outputFileName: String)
    override suspend fun stopRecording(): String
}
