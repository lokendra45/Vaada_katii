package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue

sealed class EditTenantAction {
    data class OnNameChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnPhoneChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnEmailChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnRentChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnUnitNumberChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnUnitSelected(val unitName: String) : EditTenantAction()
    data class OnMoveInDateChanged(val date: String) : EditTenantAction()
    data class OnLeaseDurationSelected(val duration: String) : EditTenantAction()
    data class OnSecurityDepositChanged(val value: TextFieldValue) : EditTenantAction()
    data class OnPropertySelected(val propertyId: String) : EditTenantAction()
    data class OnStatusSelected(val status: String) : EditTenantAction()
    /** Fired when the user picks a document or image via FileKit. [name] is the display name. */
    data class OnDocumentPicked(val name: String, val bytes: ByteArray) : EditTenantAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as OnDocumentPicked

            if (name != other.name) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }

    data class OnProfileImagePicked(val name: String, val bytes: ByteArray) : EditTenantAction() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as OnProfileImagePicked

            if (name != other.name) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }

    data class OnDocumentTypeSelected(val type: String) : EditTenantAction()
    data class OnWifiToggled(val enabled: Boolean) : EditTenantAction()
    data class OnWaterToggled(val enabled: Boolean) : EditTenantAction()
    data class OnElectricityToggled(val enabled: Boolean) : EditTenantAction()
    data class OnWasteToggled(val enabled: Boolean) : EditTenantAction()
    data class OnPaymentDueDateChanged(val date: String) : EditTenantAction()

    data object OnSaveClicked : EditTenantAction()
    object OnSuccessDialogDismissed : EditTenantAction()
    object OnBackClicked : EditTenantAction()
    object OnDeleteClicked : EditTenantAction()
    object OnDeleteDismissed : EditTenantAction()
    object OnDeleteConfirmed : EditTenantAction()
}