package com.gaatho.rent.core.utils

expect suspend fun generateTenantPdf(
    tenantName: String,
    profileInfo: String,
    rentInfo: String
): ByteArray
