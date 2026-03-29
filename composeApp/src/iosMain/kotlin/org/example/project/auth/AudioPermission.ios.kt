package org.example.project.auth

import androidx.compose.runtime.Composable

@Composable
actual fun rememberAudioPermissionState(): PermissionState {
    // iOS: assume granted for now; implement AVAudioSession permission later
    return PermissionState(
        isGranted = true,
        requestPermission = { }
    )
}
