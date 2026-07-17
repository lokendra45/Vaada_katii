package com.gaatho.rent.features.paywall.presentation

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.features.paywall.data.repository.PaywallRepository
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
}

// ─── Side Effects ─────────────────────────────────────────────────────────────

sealed interface PaywallSideEffect {
    data object NavigateBack : PaywallSideEffect
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
        }
    }

    private fun observePremiumStatus() = intent(registerIdling = false) {
        paywallRepository.isPremium.collect { premium ->
            reduce { state.copy(isPremium = premium) }
        }
    }
}
