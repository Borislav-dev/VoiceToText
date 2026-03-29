package org.example.project.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String = "",
    val title: String = "",
    val content: String,
    val createdAt: Long = 0L,
    val language: String? = null,
    val durationSeconds: Int? = null,
    val durationFormatted: String? = null,
    @SerialName("audio_url")
    val audioUrl: String? = null
)
