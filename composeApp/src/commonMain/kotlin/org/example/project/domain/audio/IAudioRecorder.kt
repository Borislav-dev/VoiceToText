package org.example.project.domain.audio

interface IAudioRecorder {
    suspend fun startRecording(outputFileName: String)
    suspend fun stopRecording(): String
}
