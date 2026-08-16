package com.gaatho.rent.features.dashboard.data

import com.gaatho.rent.core.network.safeSupabaseRead
import com.gaatho.rent.features.dashboard.data.dto.DashboardSummaryDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class CloudDashboardRepository(
    private val supabase: SupabaseClient,
    private val json: Json
) : DashboardRepository {

    override fun getDashboardSummary(ownerId: String): Flow<DashboardSummaryDto> =
        safeSupabaseRead(DashboardSummaryDto(), "CloudDashboardRepository.getDashboardSummary") {
            val params = buildJsonObject {
                put("p_owner_id", ownerId)
            }
            val result = supabase.postgrest.rpc("get_dashboard_summary", params)
            json.decodeFromString(DashboardSummaryDto.serializer(), result.data)
        }
}