package org.example.project.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import org.example.project.auth.getGoogleIdToken
import org.example.project.domain.repository.IAuthRepository

class SupabaseAuthRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : IAuthRepository {

    override suspend fun signInWithGoogle(activityContext: Any?): Result<Unit> {
        return try {
            // 1. Get Google ID Token via native implementation (Credential Manager on Android)
            val googleIdToken = getGoogleIdToken(activityContext)

            // 2. Exchange the ID Token with Supabase Auth
            supabaseClient.auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
