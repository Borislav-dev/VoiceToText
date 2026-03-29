package org.example.project.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.model.UserProfile
import org.example.project.domain.repository.IProfileRepository

sealed interface SettingsState {
    data object Loading : SettingsState
    data class Success(val profile: UserProfile) : SettingsState
    data class Error(val message: String) : SettingsState
}

class SettingsViewModel(
    private val profileRepository: IProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _state.value = SettingsState.Loading
        viewModelScope.launch {
            profileRepository.getUserProfile()
                .onSuccess { _state.value = SettingsState.Success(it) }
                .onFailure { _state.value = SettingsState.Error(it.message ?: "Failed to load profile") }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            profileRepository.deleteAccount()
                .onSuccess { onSuccess() }
                .onFailure { _state.value = SettingsState.Error(it.message ?: "Failed to delete account") }
        }
    }
}
