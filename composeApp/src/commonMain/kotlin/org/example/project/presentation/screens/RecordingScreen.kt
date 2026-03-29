package org.example.project.presentation.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import voicetotext.composeapp.generated.resources.*
import org.example.project.auth.rememberAudioPermissionState
import org.example.project.presentation.viewmodels.RecordingState
import org.example.project.presentation.viewmodels.RecordingViewModel

@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel,
    onTranscriptionSuccess: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val permissionState = rememberAudioPermissionState()
    var title by remember { mutableStateOf("") }
    val trackedKeywords by viewModel.trackedKeywords.collectAsState()
    val recordingSeconds by viewModel.recordingSeconds.collectAsState()
    val showTimeLimitDialog by viewModel.showTimeLimitDialog.collectAsState()
    val showPaywallDialog by viewModel.showPaywallDialog.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val freeRecordsLeft by viewModel.freeRecordsLeft.collectAsState()

    // Navigate on success
    LaunchedEffect(state) {
        if (state is RecordingState.Success) {
            onTranscriptionSuccess((state as RecordingState.Success).noteId)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val currentState = state) {
            is RecordingState.Idle -> IdleContent(
                title = title,
                onTitleChange = { title = it },
                trackedKeywords = trackedKeywords,
                onTrackedKeywordsChange = { viewModel.updateTrackedKeywords(it) },
                onStartRecording = {
                    if (permissionState.isGranted) {
                        viewModel.startRecording()
                    } else {
                        permissionState.requestPermission()
                    }
                },
                isPremium = isPremium,
                freeRecordsLeft = freeRecordsLeft
            )

            is RecordingState.Recording -> RecordingContent(
                recordingSeconds = recordingSeconds,
                formattedTime = viewModel.formatSeconds(recordingSeconds),
                onStopRecording = { viewModel.stopAndProcessRecording(title) }
            )

            is RecordingState.Transcribing -> TranscribingContent()

            is RecordingState.Success -> {
                // Handled by LaunchedEffect above
            }

            is RecordingState.Error -> ErrorContent(
                message = currentState.message,
                onRetry = { viewModel.resetState() }
            )
        }
    }

    if (showTimeLimitDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTimeLimitDialog() },
            title = {
                Text(stringResource(Res.string.record_limit_title))
            },
            text = {
                Text(stringResource(Res.string.record_limit_text))
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.dismissTimeLimitDialog()
                    onNavigateToPaywall()
                }) {
                    Text(stringResource(Res.string.record_upgrade_premium))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    viewModel.dismissTimeLimitDialog()
                }) {
                    Text(stringResource(Res.string.record_view_result))
                }
            }
        )
    }

    // Paywall Dialog
    if (showPaywallDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPaywallDialog() },
            title = {
                Text(stringResource(Res.string.record_paywall_title))
            },
            text = {
                Text(stringResource(Res.string.record_paywall_text))
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.dismissPaywallDialog()
                    onNavigateToPaywall()
                }) {
                    Text(stringResource(Res.string.record_upgrade))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    viewModel.dismissPaywallDialog()
                }) {
                    Text(stringResource(Res.string.record_maybe_later))
                }
            }
        )
    }
}

@Composable
private fun IdleContent(
    title: String,
    onTitleChange: (String) -> Unit,
    trackedKeywords: String,
    onTrackedKeywordsChange: (String) -> Unit,
    onStartRecording: () -> Unit,
    isPremium: Boolean,
    freeRecordsLeft: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        Text(
            text = stringResource(Res.string.record_tap_to_record),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(Res.string.record_title_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = trackedKeywords,
            onValueChange = onTrackedKeywordsChange,
            label = { Text(stringResource(Res.string.record_keywords_hint)) },
            placeholder = { Text(stringResource(Res.string.record_keywords_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        LargeFloatingActionButton(
            onClick = onStartRecording,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(Res.string.record_start_desc),
                modifier = Modifier.size(36.dp)
            )
        }

        if (!isPremium) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.record_free_records_left, freeRecordsLeft),
                style = MaterialTheme.typography.bodySmall,
                color = if (freeRecordsLeft > 0)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun RecordingContent(
    recordingSeconds: Int,
    formattedTime: String,
    onStopRecording: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Timer text turns red in the last 30 seconds
    val timerColor = if (recordingSeconds > 270) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing red recording indicator
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Live timer display
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displaySmall,
            color = timerColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.record_recording_status),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStopRecording,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = stringResource(Res.string.record_stop_desc)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.record_stop_and_transcribe))
        }
    }
}

@Composable
private fun TranscribingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 5.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.record_transcribing_status),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.record_transcribing_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = stringResource(Res.string.record_error_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text(stringResource(Res.string.action_try_again))
        }
    }
}
