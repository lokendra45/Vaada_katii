package com.gaatho.rent.features.property.presentation.add

import androidx.lifecycle.SavedStateHandle
import com.gaatho.rent.core.mvi.MviViewModel
import com.gaatho.rent.core.ui.ErrorMessageExtractor
import com.gaatho.rent.features.property.data.repository.PropertyRepository
import com.gaatho.rent.features.property.domain.model.Property
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import org.orbitmvi.orbit.viewmodel.orbitContainer
import com.skydoves.sandwich.ApiResponse

class AddPropertyViewModel(
    private val repository: PropertyRepository,
    private val supabase: SupabaseClient,
    savedStateHandle: SavedStateHandle
) : MviViewModel<AddPropertyState, AddPropertySideEffect, AddPropertyAction>() {

    private val ownerId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: ""

    override val container = orbitContainer<AddPropertyState, AddPropertySideEffect>(
        initialState = AddPropertyState(),
        savedStateHandle = savedStateHandle,
        serializer = AddPropertyState.serializer()
    )

    override fun onAction(action: AddPropertyAction) {
        when (action) {
            is AddPropertyAction.OnNameChanged -> handleNameChanged(action.name)
            is AddPropertyAction.OnAddressChanged -> handleAddressChanged(action.address)
            is AddPropertyAction.OnSaveClicked -> handleSave()
        }
    }

    private fun handleNameChanged(name: String) = intent {
        reduce { state.copy(name = name, nameError = null) }
    }

    private fun handleAddressChanged(address: String) = intent {
        reduce { state.copy(address = address, addressError = null) }
    }

    private fun handleSave() = intent {
        val currentState = state
        var hasError = false

        if (currentState.name.isBlank()) {
            reduce { state.copy(nameError = "Property name is required") }
            hasError = true
        }
        
        if (currentState.address.isBlank()) {
            reduce { state.copy(addressError = "Property address is required") }
            hasError = true
        }

        if (hasError) return@intent

        reduce { state.copy(isSaving = true) }

        val property = Property(
            id = "temp-id", // Supabase handles ID generation server-side
            ownerId = ownerId,
            name = currentState.name.trim(),
            address = currentState.address.trim(),
            propertyType = currentState.propertyType
        )

        when (val response = repository.createProperty(property)) {
            is ApiResponse.Success -> {
                postSideEffect(AddPropertySideEffect.NavigateBack)
            }
            is ApiResponse.Failure.Error -> {
                reduce { state.copy(isSaving = false) }
                val errorMsg = ErrorMessageExtractor.extract(response, "Failed to save property. You might be offline.")
                postSideEffect(AddPropertySideEffect.ShowSnackbar(errorMsg))
            }
            is ApiResponse.Failure.Exception -> {
                reduce { state.copy(isSaving = false) }
                val errorMsg = ErrorMessageExtractor.extract(response, "Failed to save property. You might be offline.")
                postSideEffect(AddPropertySideEffect.ShowSnackbar(errorMsg))
            }
        }
    }
}
