package com.gaatho.rent.features.tenant.presentation.edit

import androidx.compose.ui.text.input.TextFieldValue
import com.gaatho.rent.core.auth.SessionManager
import com.gaatho.rent.core.logging.AppLogger
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.network.StorageRepository
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.core.utils.ValidationUtil
import com.gaatho.rent.core.utils.compressImage
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.usecase.DeleteTenantUseCase
import com.gaatho.rent.features.tenant.domain.usecase.ObserveTenantUseCase
import com.gaatho.rent.features.tenant.domain.usecase.SaveTenantUseCase
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import org.orbitmvi.orbit.viewmodel.orbitContainer

class EditTenantViewModel(
    private val tenantId: String,
    private val tenantRepository: TenantRepository,
    private val observeTenant: ObserveTenantUseCase,
    private val saveTenant: SaveTenantUseCase,
    private val deleteTenant: DeleteTenantUseCase,
    private val propertyRepository: PropertyRepository,
    private val sessionManager: SessionManager,
    private val storageRepository: StorageRepository
) : MviViewModel<EditTenantState, EditTenantSideEffect, EditTenantAction>() {
    private val ownerId: String
        get() = (sessionManager.currentUserId() ?: "")

    override val container = orbitContainer<EditTenantState, EditTenantSideEffect>(EditTenantState()) {
        loadTenant()
    }

    private fun loadTenant() = intent {
        reduce { state.copy(isLoading = true) }

        try {
            val properties = propertyRepository.getProperties(ownerId).firstOrNull() ?: emptyList()
            val propertyOptions = properties.map { prop ->
                PropertyOption(
                    id = prop.id,
                    name = prop.name,
                    monthlyRent = prop.monthlyRent,
                    wifiCharge = prop.wifiCharge,
                    waterCharge = prop.waterCharge,
                    electricityCharge = prop.electricityCharge,
                    wasteCharge = prop.wasteCharge,
                    units = prop.units.map { unit ->
                        PropertyUnitOption(
                            id = unit.id,
                            name = unit.name,
                            monthlyRent = unit.monthlyRent
                        )
                    }
                )
            }

            if (tenantId == "new") {
                if (propertyOptions.isEmpty()) {
                    postSideEffect(EditTenantSideEffect.ShowSnackbar("Please add at least one property before adding a tenant."))
                    postSideEffect(EditTenantSideEffect.NavigateBack)
                    return@intent
                }
                reduce {
                    state.copy(
                        isLoading = false,
                        propertyOptions = propertyOptions
                    )
                }
            } else {
                val tenant = observeTenant(tenantId).firstOrNull()
                if (tenant != null) {
                    reduce {
                        state.copy(
                            isLoading = false,
                            name = TextFieldValue(tenant.name),
                            phone = TextFieldValue(tenant.phone ?: ""),
                            email = TextFieldValue(tenant.email ?: ""),
                            rentAmount = TextFieldValue(tenant.rentAmount.toString()),
                            propertyId = tenant.propertyId ?: "",
                            unitNumber = TextFieldValue(tenant.roomNumber ?: ""),
                            status = tenant.status,
                            propertyOptions = propertyOptions,
                            profileImageUrl = tenant.profileImageUrl,
                            documentType = tenant.documentType ?: "Citizenship",
                            documentUrl = tenant.documentUrl,
                            hasWifi = tenant.hasWifi,
                            hasWater = tenant.hasWater,
                            hasElectricity = tenant.hasElectricity,
                            hasWaste = tenant.hasWaste,
                            moveInDate = tenant.moveInDate ?: "",
                            leaseDuration = tenant.leaseDuration ?: "1 Year",
                            paymentDueDate = tenant.paymentDueDate ?: "",
                            securityDeposit = TextFieldValue(tenant.securityDeposit.toString()),
                            uploadedDocumentName = tenant.documentUrl?.substringAfterLast("/"),
                            originalCreatedAt = tenant.createdAt
                        )
                    }
                } else {
                    reduce { state.copy(isLoading = false, propertyOptions = propertyOptions) }
                    postSideEffect(EditTenantSideEffect.ShowSnackbar("Couldn't load tenant. Please try again."))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLogger.network.e(e) { "EditTenant load failed" }
            reduce { state.copy(isLoading = false) }
            postSideEffect(EditTenantSideEffect.ShowSnackbar("Couldn't load tenant. Please try again."))
        }
    }

    override fun onAction(action: EditTenantAction) {
        when (action) {
            is EditTenantAction.OnNameChanged -> intent {
                val err = if (action.value.text.isBlank()) "Name cannot be empty" else null
                reduce { state.copy(name = action.value, nameError = err) }
            }
            is EditTenantAction.OnPhoneChanged -> intent {
                val phone = action.value.text.trim()
                val err = if (phone.isBlank()) "Phone number is required" else if (!ValidationUtil.isValidNepaliPhone(phone)) "Enter a valid 10-digit Nepali phone number" else null
                reduce { state.copy(phone = action.value, phoneError = err) }
            }
            is EditTenantAction.OnEmailChanged -> intent {
                val email = action.value.text.trim()
                val err = if (email.isNotBlank() && !ValidationUtil.isValidEmail(email)) "Enter a valid email address" else null
                reduce { state.copy(email = action.value, emailError = err) }
            }
            is EditTenantAction.OnRentChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                val err = if (digits.isBlank()) "Rent cannot be empty" else null
                reduce { state.copy(rentAmount = action.value.copy(text = digits), rentError = err) }
            }
            is EditTenantAction.OnUnitNumberChanged -> intent {
                reduce { state.copy(unitNumber = action.value) }
            }
            is EditTenantAction.OnUnitSelected -> intent {
                val property = state.propertyOptions.find { it.id == state.propertyId }
                val unit = property?.units?.find { it.name == action.unitName }
                
                reduce { 
                    state.copy(
                        unitNumber = TextFieldValue(action.unitName),
                        rentAmount = if (unit != null) TextFieldValue(unit.monthlyRent.toString()) else state.rentAmount
                    )
                }
            }
            is EditTenantAction.OnMoveInDateChanged -> intent {
                reduce { state.copy(moveInDate = action.date) }
            }
            is EditTenantAction.OnLeaseDurationSelected -> intent {
                reduce { state.copy(leaseDuration = action.duration) }
            }
            is EditTenantAction.OnSecurityDepositChanged -> intent {
                val digits = action.value.text.filter { it.isDigit() }
                reduce { state.copy(securityDeposit = action.value.copy(text = digits)) }
            }
            is EditTenantAction.OnPropertySelected -> intent {
                val property = state.propertyOptions.find { it.id == action.propertyId }
                val firstUnit = property?.units?.firstOrNull()
                reduce { 
                    state.copy(
                        propertyId = action.propertyId,
                        unitNumber = TextFieldValue(firstUnit?.name ?: ""),
                        rentAmount = TextFieldValue((firstUnit?.monthlyRent ?: property?.monthlyRent ?: 0L).toString())
                    ) 
                }
            }
            is EditTenantAction.OnStatusSelected -> intent {
                reduce { state.copy(status = action.status) }
            }
            is EditTenantAction.OnProfileImagePicked -> intent {
                // Store bytes locally — upload on Save
                val compressedBytes = compressImage(action.bytes, action.name)
                reduce {
                    state.copy(
                        pendingProfileBytes = compressedBytes,
                        pendingProfileName = action.name
                    )
                }
            }
            is EditTenantAction.OnDocumentTypeSelected -> intent {
                reduce { state.copy(documentType = action.type) }
            }
            is EditTenantAction.OnPaymentDueDateChanged -> intent {
                reduce { state.copy(paymentDueDate = action.date) }
            }
            is EditTenantAction.OnWifiToggled -> handleUtilityToggle(action.enabled, "wifi")
            is EditTenantAction.OnWaterToggled -> handleUtilityToggle(action.enabled, "water")
            is EditTenantAction.OnElectricityToggled -> handleUtilityToggle(action.enabled, "electricity")
            is EditTenantAction.OnWasteToggled -> handleUtilityToggle(action.enabled, "waste")
            is EditTenantAction.OnDocumentPicked -> intent {
                // Store bytes locally — upload on Save
                val compressedBytes = compressImage(action.bytes, action.name)
                reduce {
                    state.copy(
                        uploadedDocumentName = action.name,
                        pendingDocBytes = compressedBytes,
                        pendingDocName = action.name
                    )
                }
            }
            is EditTenantAction.OnSaveClicked -> saveTenant()
            is EditTenantAction.OnSuccessDialogDismissed -> intent {
                reduce { state.copy(showSuccessDialog = false) }
                postSideEffect(EditTenantSideEffect.NavigateBack)
            }
            is EditTenantAction.OnBackClicked -> intent {
                postSideEffect(EditTenantSideEffect.NavigateBack)
            }
            is EditTenantAction.OnDeleteClicked -> intent {
                reduce { state.copy(showDeleteConfirm = true) }
            }
            is EditTenantAction.OnDeleteDismissed -> intent {
                reduce { state.copy(showDeleteConfirm = false) }
            }
            is EditTenantAction.OnDeleteConfirmed -> deleteTenant()
        }
    }

    private fun deleteTenant() = intent {
        reduce { state.copy(showDeleteConfirm = false, isSaving = true) }
        when (val response = deleteTenant(tenantId)) {
            is ApiResponse.Success -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditTenantSideEffect.NavigateToTenantList)
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditTenantSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(response, "Couldn't remove tenant. Please try again.")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditTenantSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(response, "Couldn't remove tenant. Please try again.")
                ))
            }
        }
    }

    private fun saveTenant() = intent {
        val currentState = state
        if (currentState.isSaving) return@intent
        
        var hasError = false
        var nameErr: String? = null
        var phoneErr: String? = null
        var emailErr: String? = null
        var rentErr: String? = null

        if (currentState.name.text.isBlank()) {
            nameErr = "Name cannot be empty"
            hasError = true
        }
        val phone = currentState.phone.text.trim()
        if (phone.isBlank()) {
            phoneErr = "Phone number is required"
            hasError = true
        } else if (!ValidationUtil.isValidNepaliPhone(phone)) {
            phoneErr = "Enter a valid 10-digit Nepali phone number"
            hasError = true
        }
        val email = currentState.email.text.trim()
        if (email.isNotBlank() && !ValidationUtil.isValidEmail(email)) {
            emailErr = "Enter a valid email address"
            hasError = true
        }
        if (currentState.rentAmount.text.isBlank()) {
            rentErr = "Rent cannot be empty"
            hasError = true
        }

        if (hasError) {
            reduce { state.copy(nameError = nameErr, phoneError = phoneErr, emailError = emailErr, rentError = rentErr) }
            return@intent
        }

        reduce { state.copy(isSaving = true) }

        // Backend uniqueness validation
        if (email.isNotBlank() || phone.isNotBlank()) {
            val excludeId = if (tenantId == "new") null else tenantId
            val duplicateTenant = try {
                tenantRepository.findDuplicateContact(ownerId, email, phone, excludeId)
            } catch (e: CancellationException) {
                reduce { state.copy(isSaving = false) }
                throw e
            } catch (e: Exception) {
                null // network failure — let the save proceed rather than block
            }
            if (duplicateTenant != null) {
                var dupEmailErr: String? = null
                var dupPhoneErr: String? = null
                if (!duplicateTenant.email.isNullOrBlank() && (duplicateTenant.email.equals(email, ignoreCase = true) || duplicateTenant.email.lowercase().contains(email.lowercase()))) {
                    dupEmailErr = "Email is already in use by another tenant"
                }
                val dupPhoneDigits = duplicateTenant.phone?.filter { it.isDigit() } ?: ""
                val currentPhoneDigits = phone.filter { it.isDigit() }
                if (dupPhoneDigits.isNotBlank() && (dupPhoneDigits == currentPhoneDigits || dupPhoneDigits.endsWith(currentPhoneDigits) || currentPhoneDigits.endsWith(dupPhoneDigits))) {
                    dupPhoneErr = "Phone number is already in use by another tenant"
                }
                if (dupEmailErr == null && dupPhoneErr == null) {
                    if (email.isNotBlank()) dupEmailErr = "Email is already in use by another tenant"
                    else dupPhoneErr = "Phone number is already in use by another tenant"
                }
                reduce { state.copy(isSaving = false, emailError = dupEmailErr, phoneError = dupPhoneErr) }
                postSideEffect(EditTenantSideEffect.ShowSnackbar("A tenant with this email or phone number already exists."))
                return@intent
            }
        }

        val propertyName = currentState.propertyOptions
            .find { it.id == currentState.propertyId }?.name ?: ""

        // Upload profile image if staged
        val finalProfileUrl: String = if (currentState.pendingProfileBytes != null) {
            val path = "${UuidUtil.generateV7String()}_${currentState.pendingProfileName ?: "profile.jpg"}"
            when (val r = storageRepository.uploadFile("avatars", path, currentState.pendingProfileBytes)) {
                is ApiResponse.Success -> r.data
                is ApiResponse.Failure.Error -> {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(EditTenantSideEffect.ShowSnackbar("Failed to upload profile image"))
                    return@intent
                }
                is ApiResponse.Failure.Exception -> {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(EditTenantSideEffect.ShowSnackbar("Failed to upload profile image"))
                    return@intent
                }
            }
        } else currentState.profileImageUrl ?: ""

        // Upload document if staged
        val finalDocUrl: String = if (currentState.pendingDocBytes != null) {
            val path = "${UuidUtil.generateV7String()}_${currentState.pendingDocName ?: "doc.jpg"}"
            when (val r = storageRepository.uploadFile("documents", path, currentState.pendingDocBytes)) {
                is ApiResponse.Success -> r.data
                is ApiResponse.Failure.Error -> {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(EditTenantSideEffect.ShowSnackbar("Failed to upload document"))
                    return@intent
                }
                is ApiResponse.Failure.Exception -> {
                    reduce { state.copy(isSaving = false) }
                    postSideEffect(EditTenantSideEffect.ShowSnackbar("Failed to upload document"))
                    return@intent
                }
            }
        } else currentState.documentUrl ?: ""

        val params = SaveTenantUseCase.Params(
            isNew = tenantId == "new",
            existingId = tenantId,
            ownerId = ownerId,
            name = currentState.name.text,
            email = currentState.email.text,
            phone = currentState.phone.text,
            propertyId = currentState.propertyId ?: "",
            propertyName = propertyName,
            unitNumber = currentState.unitNumber.text.trim(),
            rentAmount = currentState.rentAmount.text.toLongOrNull() ?: 0L,
            profileImageUrl = finalProfileUrl,
            documentType = currentState.documentType,
            documentUrl = finalDocUrl,
            hasWifi = currentState.hasWifi,
            hasWater = currentState.hasWater,
            hasElectricity = currentState.hasElectricity,
            hasWaste = currentState.hasWaste,
            leaseDuration = currentState.leaseDuration,
            moveInDate = currentState.moveInDate,
            paymentDueDate = currentState.paymentDueDate,
            securityDeposit = currentState.securityDeposit.text.toLongOrNull() ?: 0L,
            status = currentState.status,
            originalCreatedAt = currentState.originalCreatedAt
        )

        when (val response = saveTenant(params)) {
            is ApiResponse.Success -> {
                reduce { state.copy(isSaving = false, showSuccessDialog = true) }
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditTenantSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(response, "Couldn't save tenant. Please try again.")
                ))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                postSideEffect(EditTenantSideEffect.ShowSnackbar(
                    ErrorMessageExtractor.extract(response, "Couldn't save tenant. Please try again.")
                ))
            }
        }
    }

    private fun handleUtilityToggle(enabled: Boolean, utilityType: String) = intent {
        val property = state.propertyOptions.find { it.id == state.propertyId } ?: return@intent
        val charge = when (utilityType) {
            "wifi" -> property.wifiCharge
            "water" -> property.waterCharge
            "electricity" -> property.electricityCharge
            "waste" -> property.wasteCharge
            else -> 0L
        }
        if (charge > 0) {
            val currentRent = state.rentAmount.text.toLongOrNull() ?: 0L
            val newRent = if (enabled) currentRent + charge else (currentRent - charge).coerceAtLeast(0L)
            reduce { state.copy(rentAmount = state.rentAmount.copy(text = newRent.toString())) }
        }
        
        reduce { 
            when (utilityType) {
                "wifi" -> state.copy(hasWifi = enabled)
                "water" -> state.copy(hasWater = enabled)
                "electricity" -> state.copy(hasElectricity = enabled)
                "waste" -> state.copy(hasWaste = enabled)
                else -> state
            }
        }
    }
}
