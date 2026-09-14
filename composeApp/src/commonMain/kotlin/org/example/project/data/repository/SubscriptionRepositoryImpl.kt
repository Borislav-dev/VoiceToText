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
            val response = supabaseClient.from(TABLE_NAME)
                .select {
                    filter { eq("user_id", userId) }
                }
            
            // ВАЖНО: Вижте това в Logcat
            println("SUBSCRIPTION_DEBUG: Raw Data from Supabase: ${response.data}")

            val subscription = response.decodeSingleOrNull<UserSubscription>()
            
            if (subscription == null) {
                println("SUBSCRIPTION_DEBUG: No record found. Creating default for $userId")
                val defaultSub = UserSubscription(userId = userId, isPremium = false, freeRecordsLeft = 3)
                // Опит за създаване на запис, ако липсва
                try { supabaseClient.from(TABLE_NAME).insert(defaultSub) } catch(e: Exception) {}
                defaultSub
            } else {
                println("SUBSCRIPTION_DEBUG: Loaded successfully. isPremium = ${subscription.isPremium}")
                subscription
            }
        } catch (e: Exception) {
            println("SUBSCRIPTION_DEBUG: Error fetching subscription: ${e.message}")
            e.printStackTrace()
            UserSubscription(userId = userId, isPremium = false, freeRecordsLeft = 3)
        }
    }

    override suspend fun decrementFreeRecord(): Result<Unit> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated")

        val response = supabaseClient.from(TABLE_NAME).select { filter { eq("user_id", userId) } }
        val current = response.decodeSingleOrNull<UserSubscription>() ?: return@runCatching

        if (!current.isPremium) {
            val newCount = (current.freeRecordsLeft - 1).coerceAtLeast(0)
            supabaseClient.from(TABLE_NAME).update({ 
                set("free_records_left", newCount) 
            }) {
                filter { eq("user_id", userId) }
            }
        }
    }
}
