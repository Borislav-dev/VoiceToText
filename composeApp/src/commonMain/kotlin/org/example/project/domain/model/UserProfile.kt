package org.example.project.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val plan: String = "free",
    @SerialName("recordings_used") val recordingsUsed: Int = 0
)
