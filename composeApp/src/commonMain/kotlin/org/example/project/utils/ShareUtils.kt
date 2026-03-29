package org.example.project.utils

/**
 * Share text content using the platform's native share dialog.
 * @param context Platform-specific context (Android Context as Any?)
 * @param text The text content to share
 */
expect fun shareText(context: Any?, text: String)
