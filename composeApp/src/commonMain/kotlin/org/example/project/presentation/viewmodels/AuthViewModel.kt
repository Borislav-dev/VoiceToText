package org.example.project.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.repository.IAuthRepository

class AuthViewModel(
    private val authRepository: IAuthRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Listen to session status from Supabase direct client as it manages the session globally
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated,
                    is SessionStatus.NotAuthenticated -> {
                        _isLoading.value = false
                    }
                    else -> {
                        // SessionStatus.Initializing or LoadingFromStorage — keep loading
                    }
                }
            }
        }
    }

    fun onGoogleSignInClicked(activityContext: Any?) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val result = authRepository.signInWithGoogle(activityContext)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Sign-in failed"
                _isLoading.value = false
            }
            // On success, the sessionStatus flow in init{} will become Authenticated and stop _isLoading,
            // or the UI will navigate away based on SessionStatus.Authenticated.
        }
    }
}
