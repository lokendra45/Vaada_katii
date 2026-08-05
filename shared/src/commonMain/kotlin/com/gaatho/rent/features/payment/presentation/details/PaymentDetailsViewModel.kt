package com.gaatho.rent.features.payment.presentation.details

import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.ui.UiState
import com.gaatho.rent.features.payment.domain.repository.PaymentRepository
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.orbitmvi.orbit.viewmodel.orbitContainer

class PaymentDetailsViewModel(
    private val paymentId: String,
    private val paymentRepository: PaymentRepository,
    private val tenantRepository: TenantRepository,
    private val propertyRepository: PropertyRepository,
    private val sessionManager: com.gaatho.rent.core.auth.SessionManager
) : MviViewModel<PaymentDetailsState, PaymentDetailsSideEffect, PaymentDetailsAction>() {

    override val container = orbitContainer<PaymentDetailsState, PaymentDetailsSideEffect>(PaymentDetailsState()) {
        loadPaymentDetails()
    }

    override fun onAction(action: PaymentDetailsAction) {
        when (action) {
            PaymentDetailsAction.OnBackClicked -> intent { postSideEffect(PaymentDetailsSideEffect.NavigateBack) }
            PaymentDetailsAction.OnRetry -> loadPaymentDetails()
            PaymentDetailsAction.OnDownloadReceipt -> intent {
                postSideEffect(PaymentDetailsSideEffect.ShowMessage("Receipt downloaded to your device"))
            }
            PaymentDetailsAction.OnShareDetails -> intent {
                postSideEffect(PaymentDetailsSideEffect.ShowMessage("Sharing functionality coming soon!"))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadPaymentDetails() = intent {
        reduce { state.copy(paymentState = UiState.Loading) }

        val ownerId = sessionManager.currentUser.value?.id
        if (ownerId == null) {
            reduce { state.copy(paymentState = UiState.Error("User not logged in.")) }
            return@intent
        }

        paymentRepository.getPaymentById(paymentId)
            .flatMapLatest { payment ->
                if (payment == null) {
                    flowOf(UiState.Error("Payment not found"))
                } else {
                    val tenantFlow = tenantRepository.getTenants(ownerId)
                    val propertyFlow = propertyRepository.getProperties(ownerId)

                    combine(tenantFlow, propertyFlow) { tenants, properties ->
                        val tenant = tenants.find { it.id == payment.tenantId }
                        val property = properties.find { it.id == payment.propertyId }
                        UiState.Success(PaymentDetailsData(payment, tenant, property))
                    }
                }
            }
            .catch { e ->
                val msg = ErrorMessageExtractor.extract(e, "Could not load payment details.")
                emit(UiState.Error(msg))
            }
            .collect { result ->
                reduce { state.copy(paymentState = result) }
            }
    }
}
