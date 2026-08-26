package com.gaatho.rent.core.network

import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class ReminderResult(
    val success: Boolean,
    val message: String? = null,
    val sent_count: Int = 0
)

class ReminderRepository(
    private val supabase: SupabaseClient,
    private val json: Json
) {
    suspend fun sendRentReminders(ownerId: String): ApiResponse<ReminderResult> = runSupabaseWrite("ReminderRepository.sendRentReminders") {
        val response = supabase.functions.invoke("send-reminder") {
            setBody(buildJsonObject {
                put("owner_id", ownerId)
            })
        }
        json.decodeFromString(ReminderResult.serializer(), response.bodyAsText())
    }
}
