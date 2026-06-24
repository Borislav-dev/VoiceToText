package org.example.project.data.audio

import org.example.project.domain.audio.IAudioRecorder

actual class AudioRecorder actual constructor(context: Any?) : IAudioRecorder {

    override suspend fun startRecording(outputFileName: String) {
        throw UnsupportedOperationException("Audio recording not yet implemented for iOS")
    }

    override suspend fun stopRecording(): String {
        throw UnsupportedOperationException("Audio recording not yet implemented for iOS")
    }
}
