package com.gaatho.rent.features.property.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PropertyUnit(
    val id: String,
    val name: String,
    val monthlyRent: Long,
    val isOccupied: Boolean = false,
    val tenantId: String? = null
)
