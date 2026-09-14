package org.example.project.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSubscription(
    @SerialName("id") val id: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("free_records_left") val freeRecordsLeft: Int = 3,
    @SerialName("is_premium") val isPremium: Boolean = false
)
