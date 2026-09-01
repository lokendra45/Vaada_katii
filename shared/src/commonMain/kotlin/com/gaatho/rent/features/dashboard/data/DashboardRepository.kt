package com.gaatho.rent.features.dashboard.data

import com.gaatho.rent.features.dashboard.data.dto.DashboardSummaryDto
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getDashboardSummary(
        ownerId: String,
        startDate: String,
        endDate: String,
        prevStartDate: String,
        prevEndDate: String
    ): Flow<DashboardSummaryDto>
}