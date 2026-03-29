package org.example.project.data.repository

import com.russhwolf.settings.Settings
import org.example.project.domain.repository.IPreferencesRepository

class PreferencesRepositoryImpl(
    private val settings: Settings
) : IPreferencesRepository {

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    override fun isOnboardingCompleted(): Boolean {
        return settings.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    override fun setOnboardingCompleted(completed: Boolean) {
        settings.putBoolean(KEY_ONBOARDING_COMPLETED, completed)
    }
}
