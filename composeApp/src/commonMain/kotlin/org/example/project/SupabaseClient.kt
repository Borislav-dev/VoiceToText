package org.example.project

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.ktor.client.plugins.HttpTimeout

@OptIn(SupabaseInternal::class)
val supabaseClient: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://tgnmqssdcxfoekxfqqvj.supabase.co",
    supabaseKey = "sb_publishable_Mt7JPNfPcEskU1u99lx-IQ_2udU6pUH"

) {
    httpConfig {
        install(HttpTimeout) {
            requestTimeoutMillis = 45_000L
            connectTimeoutMillis = 45_000L
            socketTimeoutMillis = 45_000L
        }
    }
    install(Auth) {
        scheme = "voicetotext"
        host = "callback"
    }
    install(Postgrest)
    install(Storage)
}
