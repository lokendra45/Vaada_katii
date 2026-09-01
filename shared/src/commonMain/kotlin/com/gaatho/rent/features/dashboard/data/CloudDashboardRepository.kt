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

    override fun getDashboardSummary(
        ownerId: String,
        startDate: String,
        endDate: String,
        prevStartDate: String,
        prevEndDate: String
    ): Flow<DashboardSummaryDto> =
        safeSupabaseReadWithCache(
            default = DashboardSummaryDto(),
            tag = "CloudDashboardRepository.getDashboardSummary",
            cache = cache,
            cacheKey = "dashboard_summary_${ownerId}_${startDate}_${endDate}",
            serializer = DashboardSummaryDto.serializer()
        ) {
            val params = buildJsonObject {
                put("p_owner_id", ownerId)
                put("p_start_date", startDate)
                put("p_end_date", endDate)
                put("p_prev_start_date", prevStartDate)
                put("p_prev_end_date", prevEndDate)
            }
            val result = supabase.postgrest.rpc("get_dashboard_summary_v2", params)
            json.decodeFromString(DashboardSummaryDto.serializer(), result.data)
        }
}