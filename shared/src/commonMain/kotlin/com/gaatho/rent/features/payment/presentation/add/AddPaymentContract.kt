package com.gaatho.rent.features.payment.presentation.add

import com.gaatho.rent.core.ui.UiState
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.StringResource
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.serialization.Transient
import rentmanagerapp.shared.generated.resources.Res
import rentmanagerapp.shared.generated.resources.payment_method_bank_transfer
import rentmanagerapp.shared.generated.resources.payment_method_cash
import rentmanagerapp.shared.generated.resources.payment_method_esewa
import rentmanagerapp.shared.generated.resources.payment_method_khalti

data class AddPaymentState(
    @Transient val amount: TextFieldValue = TextFieldValue(),
    // All active tenants from DB (unfiltered)
    val allTenants: ImmutableList<TenantSelectionModel> = kotlinx.collections.immutable.persistentListOf(),
    // Tenants filtered by selectedPropertyId for display
    val tenantsState: UiState<ImmutableList<TenantSelectionModel>> = UiState.Loading,
    val selectedTenantId: String? = null,

    val propertiesState: UiState<ImmutableList<PropertySelectionModel>> = UiState.Loading,
    val selectedPropertyId: String? = null,
    val selectedUnit: String? = null,

    val paymentDate: String = "",
    val selectedPaymentMethod: PaymentMethod? = null,
    @Transient val remarks: TextFieldValue = TextFieldValue(),
    val isReceiptAgreed: Boolean = true,

    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val showDatePicker: Boolean = false
) {
    val canSubmit: Boolean
        get() = amount.text.isNotBlank() && amount.text != "0" &&
                selectedTenantId != null &&
                selectedPropertyId != null &&
                selectedUnit != null &&
                paymentDate.isNotBlank() &&
                selectedPaymentMethod != null &&
                isReceiptAgreed &&
                !isSaving

    /** Rent amount of the currently selected tenant, for the "Total due" label. */
    val selectedTenantRentAmount: Long?
        get() = allTenants.find { it.id == selectedTenantId }?.rentAmount
        
    val propertyItems: ImmutableList<PropertySelectionModel> get() = (propertiesState as? UiState.Success)?.data ?: kotlinx.collections.immutable.persistentListOf()
    val tenantItems: ImmutableList<TenantSelectionModel> get() = (tenantsState as? UiState.Success)?.data ?: kotlinx.collections.immutable.persistentListOf()
    
    val selectedTenant: TenantSelectionModel? get() = allTenants.find { it.id == selectedTenantId }
    val selectedProperty: PropertySelectionModel? get() = propertyItems.find { it.id == selectedPropertyId }
    
    val unitOptions: ImmutableList<String> get() = selectedProperty?.let { prop ->
        if (prop.totalUnits > 0) {
            (1..prop.totalUnits).map { "Unit $it" }.let { kotlinx.collections.immutable.persistentListOf(*it.toTypedArray()) }
        } else {
            kotlinx.collections.immutable.persistentListOf()
        }
    } ?: kotlinx.collections.immutable.persistentListOf()
}

data class TenantSelectionModel(
    val id: String,
    val name: String,
    val propertyId: String? = null,
    val rentAmount: Long = 0L,
    val roomNumber: String? = null
)

data class PropertySelectionModel(
    val id: String,
    val name: String,
    val totalUnits: Int = 1
)


enum class PaymentMethod(val labelRes: StringResource, val displayName: String) {

    CASH(Res.string.payment_method_cash, "Cash"),
    BANK_TRANSFER(Res.string.payment_method_bank_transfer, "Bank Transfer"),
    ESEWA(Res.string.payment_method_esewa, "eSewa"),
    KHALTI(Res.string.payment_method_khalti, "Khalti")
}

sealed class AddPaymentAction {
    data class OnAmountChanged(val value: TextFieldValue) : AddPaymentAction()
    data class OnTenantSelected(val id: String) : AddPaymentAction()
    data class OnPropertySelected(val id: String) : AddPaymentAction()
    data class OnUnitSelected(val unit: String) : AddPaymentAction()
    data class OnPaymentDateChanged(val date: String) : AddPaymentAction()
    data class OnPaymentMethodSelected(val method: PaymentMethod) : AddPaymentAction()
    data class OnRemarksChanged(val value: TextFieldValue) : AddPaymentAction()
    data class OnAgreementToggled(val agreed: Boolean) : AddPaymentAction()
    data object OnDateFieldClicked : AddPaymentAction()
    data object OnDatePickerDismissed : AddPaymentAction()

    data object OnRecordPaymentClicked : AddPaymentAction()
    data object OnBackClicked : AddPaymentAction()
}

sealed class AddPaymentEffect {
    data object NavigateBack : AddPaymentEffect()
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : AddPaymentEffect()
}
