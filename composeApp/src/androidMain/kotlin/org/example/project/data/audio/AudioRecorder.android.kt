package org.example.project.data.audio

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.example.project.MediaRecorderService
import org.example.project.domain.audio.IAudioRecorder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

actual class AudioRecorder actual constructor(context: Any?) : IAudioRecorder {

    private val appContext: Context = (context as Context).applicationContext

    actual override suspend fun startRecording(outputFileName: String) {
        val intent = Intent(appContext, MediaRecorderService::class.java).apply {
            action = MediaRecorderService.ACTION_START
            putExtra(MediaRecorderService.EXTRA_FILE_NAME, outputFileName)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
    }

    actual override suspend fun stopRecording(): String = withContext(Dispatchers.IO) {
        // Create a latch so we can wait for the service to finish stopping
        val latch = CountDownLatch(1)
        MediaRecorderService.stopLatch = latch

        val intent = Intent(appContext, MediaRecorderService::class.java).apply {
            action = MediaRecorderService.ACTION_STOP
        }
        appContext.startService(intent)

        // Wait up to 5 seconds for MediaRecorder.stop() + release() to complete
        // This blocks the IO thread, NOT the main thread
        latch.await(5, TimeUnit.SECONDS)

        // Suspending delay for filesystem flush (non-blocking)
        delay(300)

        MediaRecorderService.currentOutputFilePath
    }
}
