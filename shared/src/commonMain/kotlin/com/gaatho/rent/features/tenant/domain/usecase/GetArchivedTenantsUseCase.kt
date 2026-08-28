package com.gaatho.rent.features.tenant.domain.usecase

import com.gaatho.rent.core.utils.DateTimeUtil
import com.gaatho.rent.features.tenant.data.repository.TenantRepository
import com.gaatho.rent.features.tenant.domain.model.Tenant
import kotlinx.coroutines.flow.firstOrNull

class GetArchivedTenantsUseCase(
    private val tenantRepository: TenantRepository
) {
    suspend operator fun invoke(ownerId: String): List<Tenant> {
        val tenants = tenantRepository.getTenants(ownerId).firstOrNull() ?: return emptyList()
        val now = DateTimeUtil.nowInstant()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        
        return tenants.filter { tenant ->
            tenant.status.equals("Inactive", ignoreCase = true) &&
            isOlderThan30Days(tenant.updatedAt ?: tenant.createdAt ?: "", now, thirtyDaysInMillis)
        }
    }
    
    private fun isOlderThan30Days(dateStr: String, now: kotlin.time.Instant, threshold: Long): Boolean {
        return try {
            val date = kotlin.time.Instant.parse(dateStr)
            now.toEpochMilliseconds() - date.toEpochMilliseconds() > threshold
        } catch (e: Exception) {
            false
        }
    }
}
