package org.example.project.utils

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localizedStringForLanguageCode

actual fun getSystemLanguage(): String {
    val locale = NSLocale.currentLocale
    val langCode = locale.languageCode
    return locale.localizedStringForLanguageCode(langCode) ?: "English"
}

actual fun getSystemLanguageCode(): String {
    return NSLocale.currentLocale.languageCode
}
