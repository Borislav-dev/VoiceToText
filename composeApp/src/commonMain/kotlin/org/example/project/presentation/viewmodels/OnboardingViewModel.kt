package org.example.project.presentation.viewmodels

import androidx.lifecycle.ViewModel
import org.example.project.domain.repository.IPreferencesRepository

class OnboardingViewModel(
    private val preferencesRepository: IPreferencesRepository
) : ViewModel() {

    fun completeOnboarding() {
        preferencesRepository.setOnboardingCompleted(true)
    }
}
