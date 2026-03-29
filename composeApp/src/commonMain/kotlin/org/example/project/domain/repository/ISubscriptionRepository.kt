package org.example.project.domain.repository

import org.example.project.domain.model.UserSubscription

interface ISubscriptionRepository {
    suspend fun getSubscription(): Result<UserSubscription>
    suspend fun decrementFreeRecord(): Result<Unit>
}
