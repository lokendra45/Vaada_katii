package com.gaatho.rent.features.paywall.presentation

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.features.paywall.data.repository.PaywallRepository
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.viewmodel.orbitContainer

// ─── State ───────────────────────────────────────────────────────────────────

@Serializable
data class PaywallState(
    val isPremium: Boolean = false
)

// ─── Actions ─────────────────────────────────────────────────────────────────

sealed interface PaywallAction {
    data object OnDismiss : PaywallAction
    /**
     * Dispatch this after a successful WebView payment callback and
     * server-side verification to unlock the premium entitlement.
     */
    data object OnPaymentSucceeded : PaywallAction
}

// ─── Side Effects ─────────────────────────────────────────────────────────────

sealed interface PaywallSideEffect {
    data object NavigateBack : PaywallSideEffect
    data object NavigateBackWithSuccess : PaywallSideEffect
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class PaywallViewModel(
    private val paywallRepository: PaywallRepository
) : MviViewModel<PaywallState, PaywallSideEffect, PaywallAction>() {

    override val container = orbitContainer<PaywallState, PaywallSideEffect>(
        initialState = PaywallState()
    ) {
        observePremiumStatus()
    }

    override fun onAction(action: PaywallAction) {
        when (action) {
            is PaywallAction.OnDismiss -> intent {
                postSideEffect(PaywallSideEffect.NavigateBack)
            }
            is PaywallAction.OnPaymentSucceeded -> intent {
                viewModelScope.launch { paywallRepository.grantPremiumAccess() }
                postSideEffect(PaywallSideEffect.NavigateBackWithSuccess)
            }
        }
    }

    private fun observePremiumStatus() = intent(registerIdling = false) {
        paywallRepository.isPremium.collect { premium ->
            reduce { state.copy(isPremium = premium) }
        }
    }
}
