package org.example.project.domain.repository

interface IPreferencesRepository {
    fun isOnboardingCompleted(): Boolean
    fun setOnboardingCompleted(completed: Boolean)
}
