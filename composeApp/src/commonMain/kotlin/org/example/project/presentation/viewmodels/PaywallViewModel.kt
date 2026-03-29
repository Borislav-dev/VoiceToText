package org.example.project.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SubscriptionPackage {
    MONTHLY, YEARLY
}

data class PaywallState(
    val selectedPackage: SubscriptionPackage = SubscriptionPackage.YEARLY,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PaywallViewModel : ViewModel() {
    private val _state = MutableStateFlow(PaywallState())
    val state: StateFlow<PaywallState> = _state.asStateFlow()

    fun selectPackage(pkg: SubscriptionPackage) {
        _state.value = _state.value.copy(selectedPackage = pkg)
    }

    fun subscribe() {
        // TODO: Integrate actual billing logic here later
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        
        // Simulating a network request / purchase flow
        // For now, we just mock the loading state. Real implementation will handle success/failure.
    }
    
    fun restorePurchases() {
        // TODO: Integrate actual restore logic here later
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
