package com.gaatho.rent.features.tenant.domain.usecase

import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.core.utils.UuidUtil
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import com.skydoves.sandwich.ApiResponse

/**
 * Creates a new tenant or updates an existing one, depending on [Params.isNew].
 *
 * Encapsulates the entity-assembly rules that previously lived in the edit
 * ViewModel: generating the id for new tenants, denormalizing the selected
 * property's name, normalizing optional contact fields, and stamping timestamps.
 * Field-level validation (non-blank name/rent) remains the ViewModel's
 * responsibility because it is tightly coupled to form-state errors.
 */
class SaveTenantUseCase(
    private val repository: TenantRepository
) {
    suspend operator fun invoke(params: Params): ApiResponse<Unit> {
        val tenant = Tenant(
            id = if (params.isNew) UuidUtil.generateV7String() else params.existingId,
            ownerId = params.ownerId,
            name = params.name,
            email = params.email.takeIf { it.isNotBlank() },
            phone = params.phone.takeIf { it.isNotBlank() },
            propertyId = params.propertyId.takeIf { it.isNotBlank() },
            propertyName = params.propertyName.takeIf { it.isNotBlank() },
            roomNumber = params.unitNumber.takeIf { it.isNotBlank() },
            rentAmount = params.rentAmount,
            status = params.status,
            createdAt = DateTimeUtil.nowIsoString(),
            updatedAt = DateTimeUtil.nowIsoString()
        )

        return if (params.isNew) {
            repository.createTenant(tenant)
        } else {
            repository.updateTenant(tenant)
        }
    }

    data class Params(
        val isNew: Boolean,
        val existingId: String,
        val ownerId: String,
        val name: String,
        val email: String,
        val phone: String,
        val propertyId: String,
        val propertyName: String,
        val unitNumber: String,
        val rentAmount: Long,
        val status: String
    )
}
