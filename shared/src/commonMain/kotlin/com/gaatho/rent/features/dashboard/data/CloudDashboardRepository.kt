package com.gaatho.rent.features.dashboard.data

import com.gaatho.rent.core.cache.DataStoreCache
import com.gaatho.rent.core.network.safeSupabaseReadWithCache
import com.gaatho.rent.features.dashboard.data.dto.DashboardSummaryDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CloudDashboardRepository(
    private val supabase: SupabaseClient,
    private val json: Json,
    private val cache: DataStoreCache
) : DashboardRepository {

    override fun getDashboardSummary(ownerId: String): Flow<DashboardSummaryDto> =
        safeSupabaseReadWithCache(
            default = DashboardSummaryDto(),
            tag = "CloudDashboardRepository.getDashboardSummary",
            cache = cache,
            cacheKey = "dashboard_summary_$ownerId",
            serializer = DashboardSummaryDto.serializer()
        ) {
            // No parameters needed — the function uses auth.uid() internally (IDOR-safe)
            val result = supabase.postgrest.rpc("get_dashboard_summary")
            json.decodeFromString(DashboardSummaryDto.serializer(), result.data)
        }
}