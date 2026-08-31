package com.gaatho.rent.features.tenant.domain.usecase

import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.coroutines.flow.firstOrNull

class GetArchivedTenantsUseCase(
    private val tenantRepository: TenantRepository
) {
    suspend operator fun invoke(ownerId: String): List<Tenant> {
        return tenantRepository.getInactiveTenantsOlderThan30Days(ownerId).firstOrNull() ?: return emptyList()
    }
}
