package com.gaatho.rent.features.tenant.domain.usecase

import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.skydoves.sandwich.ApiResponse

/**
 * Deletes a tenant by id. ViewModels handle the resulting UI side effects
 * (navigation, error messages); this use case owns only the deletion rule.
 */
class DeleteTenantUseCase(
    private val repository: TenantRepository
) {
    suspend operator fun invoke(tenantId: String): ApiResponse<Unit> =
        repository.deleteTenant(tenantId)
}
