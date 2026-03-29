package org.example.project.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getPlatformContext(): Any? = LocalContext.current
