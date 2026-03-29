package org.example.project.data.audio

import org.example.project.domain.audio.IAudioRecorder

actual class AudioRecorder actual constructor(context: Any?) : IAudioRecorder {

    actual override suspend fun startRecording(outputFileName: String) {
        throw UnsupportedOperationException("Audio recording not yet implemented for iOS")
    }

    actual override suspend fun stopRecording(): String {
        throw UnsupportedOperationException("Audio recording not yet implemented for iOS")
    }
}
