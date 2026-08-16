package com.gaatho.rent.core.ui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A standardized sealed interface representing the different states of a UI data-loading operation.
 *
 * Annotated with `@Serializable` so that any ViewModel state class containing `UiState`
 * can be persisted by Orbit's saved state mechanism (process death recovery).
 *
 * ## Why `Throwable` was removed from `Error`
 * `Throwable` is not serializable by kotlinx.serialization, and it makes no sense
 * to persist a JVM exception across process boundaries. Error state is represented
 * solely by a human-readable [Error.message].
 * For debugging purposes, catch the `Throwable` in the ViewModel and map it through
 * [ErrorMessageExtractor.extract] before calling
 * `reduce { state.copy(propertiesState = UiState.Error(ErrorMessageExtractor.extract(e, "..."))) }` —
 * never pass `e.message` directly, it can contain raw system/backend text.
 *
 * ## `@SerialName` annotations
 * Explicit serial names protect against ProGuard/R8 class name obfuscation in release
 * builds. Without them, minification renames classes and the saved state fails to
 * deserialize on process restoration.
 *
 * @param T The type of data managed by this state.
 */
@Serializable
sealed interface UiState<out T> {

    /**
     * The initial state before any operation has started.
     */
    @Serializable
    @SerialName("idle")
    data object Idle : UiState<Nothing>

    /**
     * A background operation is currently in progress.
     */
    @Serializable
    @SerialName("loading")
    data object Loading : UiState<Nothing>

    /**
     * The operation was successful. Provides the resulting [data].
     *
     * @property data The payload returned from the operation.
     */
    @Serializable
    @SerialName("success")
    data class Success<T>(val data: T) : UiState<T>

    /**
     * The operation failed.
     *
     * @property message A human-readable, allow-listed message shown in the UI.
     *   Build it via [ErrorMessageExtractor.extract]; never pass `throwable.message`.
     */
    @Serializable
    @SerialName("error")
    data class Error(val message: String) : UiState<Nothing>
}

/**
 * Returns the data if this state is [UiState.Success], or `null` otherwise.
 */
fun <T> UiState<T>.getOrNull(): T? = (this as? UiState.Success)?.data
