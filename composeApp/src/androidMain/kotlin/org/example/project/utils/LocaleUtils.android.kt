package org.example.project.utils

import java.util.Locale

actual fun getSystemLanguage(): String {
    return Locale.getDefault().getDisplayLanguage(Locale.ENGLISH)
}

actual fun getSystemLanguageCode(): String {
    return Locale.getDefault().language
}
