package org.example.project.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import org.example.project.domain.model.UserSubscription
import org.example.project.domain.repository.ISubscriptionRepository

class SubscriptionRepositoryImpl(
    private val supabaseClient: SupabaseClient
) : ISubscriptionRepository {

    companion object {
        private const val TABLE_NAME = "user_subscriptions"
    }

    override suspend fun getSubscription(): Result<UserSubscription> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated")

        try {
            supabaseClient.from(TABLE_NAME)
                .select { filter { eq("user_id", userId) } }
                .decodeSingle<UserSubscription>()
        } catch (e: Exception) {
            // No record found — return default (3 free records, not premium)
            println("No subscription record found for user $userId, using defaults.")
            UserSubscription(userId = userId)
        }
    }

    override suspend fun decrementFreeRecord(): Result<Unit> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated")

        val current = supabaseClient.from(TABLE_NAME)
            .select { filter { eq("user_id", userId) } }
            .decodeSingle<UserSubscription>()

        val newCount = (current.freeRecordsLeft - 1).coerceAtLeast(0)

        supabaseClient.from(TABLE_NAME)
            .update({ set("free_records_left", newCount) }) {
                filter { eq("user_id", userId) }
            }
    }
}
