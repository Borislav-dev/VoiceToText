package org.example.project.auth

/**
 * Platform-specific Google Sign-In that returns a Google ID Token string.
 * On Android, this uses Credential Manager / Google Identity Services.
 * On iOS, this would use Apple's Sign in with Google SDK (stub for now).
 */
expect suspend fun getGoogleIdToken(activityContext: Any?): String
