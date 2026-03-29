package org.example.project

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MediaRecorderService : Service() {

    companion object {
        const val ACTION_START = "org.example.project.ACTION_START_RECORDING"
        const val ACTION_STOP = "org.example.project.ACTION_STOP_RECORDING"
        const val EXTRA_FILE_NAME = "extra_file_name"

        private const val CHANNEL_ID = "recording_channel"
        private const val NOTIFICATION_ID = 1001

        // Shared state for the AudioRecorder to read the file path
        @Volatile
        var currentOutputFilePath: String = ""
            private set

        @Volatile
        var isRecording: Boolean = false
            private set

        // Latch for synchronizing stop — AudioRecorder.stopRecording() blocks on this
        @Volatile
        var stopLatch: CountDownLatch? = null
    }

    private var recorder: MediaRecorder? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val fileName = intent.getStringExtra(EXTRA_FILE_NAME) ?: "recording"
                startRecording(fileName)
            }
            ACTION_STOP -> {
                stopRecording()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecording(fileName: String) {
        val outputFile = File(cacheDir, "$fileName.m4a")
        currentOutputFilePath = outputFile.absolutePath

        recorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64000)
            setAudioSamplingRate(16000)
            setOutputFile(currentOutputFilePath)
            prepare()
            start()
        }

        isRecording = true

        // Start as foreground service with notification
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
            } catch (_: Exception) {
                // Ignore if already stopped
            }
            release()
        }
        recorder = null
        isRecording = false

        // Signal that stop is complete and file is flushed
        stopLatch?.countDown()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Voice Recording",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when audio is being recorded"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("VoiceToText")
            .setContentText("Recording in progress…")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }
}
