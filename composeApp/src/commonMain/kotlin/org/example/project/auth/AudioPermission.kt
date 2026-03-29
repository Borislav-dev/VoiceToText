package org.example.project.auth

import androidx.compose.runtime.Composable

/**
 * Result of a permission request.
 */
data class PermissionState(
    val isGranted: Boolean,
    val requestPermission: () -> Unit
)

/**
 * Platform-specific composable that manages audio recording permission.
 * On Android: uses ActivityResultContracts.RequestPermission().
 * On iOS: stub (always granted for now).
 */
@Composable
expect fun rememberAudioPermissionState(): PermissionState
