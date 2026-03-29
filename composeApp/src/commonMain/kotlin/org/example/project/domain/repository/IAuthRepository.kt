package org.example.project.domain.repository

interface IAuthRepository {
    /**
     * Attempts to sign in with Google using the provided activity context.
     * On Android, this will trigger the native Credential Manager.
     */
    suspend fun signInWithGoogle(activityContext: Any?): Result<Unit>
}
