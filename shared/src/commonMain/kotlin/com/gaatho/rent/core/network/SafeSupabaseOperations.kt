package com.gaatho.rent.core.network

import com.gaatho.rent.core.logging.AppLogger
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.mapSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Runs a suspend Supabase read inside a cold [Flow] that NEVER throws to the
 * collector. On failure the exception is logged and [default] is emitted.
 *
 * This is the single safety net preventing network/parse exceptions from
 * crashing ViewModels that collect with `.firstOrNull()` or `.collect {}`
 * without their own catch block.
 *
 * Note: `kotlinx.coroutines.flow.catch` automatically rethrows
 * [CancellationException], so coroutine cancellation always propagates.
 */
fun <T> safeSupabaseRead(
    default: T,
    tag: String,
    block: suspend () -> T
): Flow<T> = flow {
    emit(block())
}.catch { e ->
    AppLogger.network.e(e) { "$tag failed; emitting safe default" }
    emit(default)
}

/**
 * Runs a suspend Supabase write and wraps the result in a Sandwich [ApiResponse].
 * Never throws to the caller — failures become [ApiResponse.Failure.Exception]
 * so ViewModels can show a user-friendly message. Cancellation is rethrown so
 * structured concurrency stays intact.
 */
suspend fun <T> runSupabaseWrite(
    tag: String,
    block: suspend () -> T
): ApiResponse<T> {
    return try {
        ApiResponse.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppLogger.network.e(e) { "$tag failed" }
        ApiResponse.Failure.Exception(e)
    }
}

/** Convenience wrapper returning [ApiResponse]`<Unit>` for writes whose payload is discarded. */
suspend fun runSupabaseWriteUnit(
    tag: String,
    block: suspend () -> Unit
): ApiResponse<Unit> = runSupabaseWrite(tag, block).mapSuccess { Unit }