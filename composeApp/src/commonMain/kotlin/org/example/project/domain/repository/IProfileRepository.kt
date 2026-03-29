package org.example.project.domain.repository

import org.example.project.domain.model.UserProfile

interface IProfileRepository {
    suspend fun getUserProfile(): Result<UserProfile>
    suspend fun incrementRecordingsUsed(): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}
