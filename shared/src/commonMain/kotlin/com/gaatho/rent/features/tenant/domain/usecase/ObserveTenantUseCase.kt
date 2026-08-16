package com.gaatho.rent.features.tenant.domain.usecase

import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.coroutines.flow.Flow

/**
 * Observes a single tenant by id as a reactive stream of domain models.
 * Replaces direct [TenantRepository.getTenantById] calls from ViewModels so the
 * data-source routing (local vs cloud) stays encapsulated behind the repository.
 */
class ObserveTenantUseCase(
    private val repository: TenantRepository
) {
    operator fun invoke(tenantId: String): Flow<Tenant?> =
        repository.getTenantById(tenantId)
}
