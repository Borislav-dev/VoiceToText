package org.example.project.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import org.example.project.domain.model.UserProfile
import org.example.project.domain.repository.IProfileRepository

class ProfileRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : IProfileRepository {

    override suspend fun getUserProfile(): Result<UserProfile> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated")

        supabaseClient.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<UserProfile>()
    }

    override suspend fun incrementRecordingsUsed(): Result<Unit> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated")

        val current = supabaseClient.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<UserProfile>()

        supabaseClient.from("profiles")
            .update({ set("recordings_used", current.recordingsUsed + 1) }) {
                filter { eq("id", userId) }
            }
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        // Securely delete the user's auth record and all cascading data via RPC
        supabaseClient.postgrest.rpc("delete_my_account")
        
        // Clear local cache and log the user out
        supabaseClient.auth.signOut()
    }
}
