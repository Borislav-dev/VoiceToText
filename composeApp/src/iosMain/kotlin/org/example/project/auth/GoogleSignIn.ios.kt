package org.example.project.auth

actual suspend fun getGoogleIdToken(activityContext: Any?): String {
    throw UnsupportedOperationException("Native Google Sign-In is not yet implemented for iOS")
}
