package org.example.project.auth

import androidx.compose.runtime.Composable

/**
 * Provides the platform-specific context (Android Context, etc.) as Any?.
 * Used by the native Google Sign-In to get the Activity context on Android.
 */
@Composable
expect fun getPlatformContext(): Any?
