package org.example.project.utils

/**
 * Returns the device's current system language display name (e.g. "English", "Български").
 */
expect fun getSystemLanguage(): String

/**
 * Returns the 2-letter ISO-639-1 language code (e.g. "en", "bg", "de").
 */
expect fun getSystemLanguageCode(): String
