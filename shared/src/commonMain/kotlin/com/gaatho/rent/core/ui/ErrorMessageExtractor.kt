package com.gaatho.rent.core.ui

import com.skydoves.sandwich.ApiResponse
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.exception.AuthSessionMissingException
import io.github.jan.supabase.auth.exception.AuthWeakPasswordException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.exceptions.UnauthorizedRestException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/**
 * A TRUE allow-list ErrorMessageExtractor: the real exception TYPE decides which
 * pre-written, friendly message is shown — but the raw .message string of a
 * random exception is NEVER read or returned. Only [UserFacingException]
 * messages (ones you wrote yourself) are ever shown verbatim.
 *
 * ORDERING IS LOAD-BEARING. Kotlin's `when (x) { is A -> ..; is B -> .. }` is
 * first-match-wins, not most-specific-type-wins. Several types below share an
 * inheritance chain, so a general branch listed above a specific one makes the
 * specific one dead code — that was the original bug (Ktor's HTTP exceptions
 * all extend IllegalStateException, so they were being caught by a generic
 * IllegalStateException branch instead of their own branches).
 */
object ErrorMessageExtractor {

    private const val GENERIC = "Something went wrong. Please try again."
    private const val NETWORK = "Network error: Unable to connect to server. Please check your internet."
    private const val TIMEOUT = "The request timed out. Please check your connection and try again."
    private const val SERVER_BUSY = "Our servers are currently busy. Please try again later."
    private const val MAINTENANCE = "We're temporarily down for maintenance. Please try again shortly."
    private const val BAD_REQUEST = "We couldn't process that request. Please check your input and try again."
    private const val SESSION_EXPIRED = "Your session has expired. Please log in again."
    private const val PERMISSION_DENIED = "You don't have permission to do that."
    private const val NOT_FOUND = "We couldn't find what you're looking for."
    private const val CONFLICT = "That already exists or conflicts with something else."
    private const val VALIDATION = "Some of the information provided isn't valid. Please check and try again."
    private const val RATE_LIMITED = "You're doing that too much. Please wait a moment and try again."
    private const val PARSE_ERROR = "We couldn't read the server's response. Please try again."
    private const val WEAK_PASSWORD = "Please choose a stronger password."

    fun extract(
        throwable: Throwable?,
        defaultMessage: String = GENERIC
    ): String {
        if (throwable == null) return defaultMessage

        // Real crashes (OOM, StackOverflow) mean the app is broken — never hide
        // that behind a friendly toast, rethrow so it actually surfaces/reports.
        if (throwable is Error) throw throwable

        return when (throwable) {

            // The ONLY branch allowed to return a message we didn't write ourselves.
            is UserFacingException -> throwable.userMessage

            // Must come before CancellationException below: in some Ktor versions
            // HttpRequestTimeoutException's internal impl surfaces alongside/as a
            // CancellationException (Ktor issue KTOR-3192). Listing it explicitly
            // guarantees a real timeout always gets TIMEOUT, regardless of version.
            is HttpRequestTimeoutException -> TIMEOUT
            is ConnectTimeoutException -> TIMEOUT
            is SocketTimeoutException -> TIMEOUT

            // Coroutine cancellation (rotation, nav away, scope cancelled) is not a
            // failure — never show it, never swallow it, just propagate it. Must be
            // above IllegalStateException below: java.util.concurrent.CancellationException
            // (what kotlinx.coroutines.CancellationException is on JVM) extends it.
            is CancellationException -> throw throwable

            // Ktor HTTP responses. Must be above the generic IllegalStateException
            // branch: ResponseException (parent of these three) extends it directly.
            is ServerResponseException -> serverErrorMessage(throwable.response.status)
            is ClientRequestException -> clientErrorMessage(throwable.response.status, defaultMessage)
            is RedirectResponseException -> defaultMessage

            // Supabase (io.github.jan.supabase.exceptions). Specific subclasses
            // listed before their parents for the same first-match-wins reason.
            is AuthWeakPasswordException -> WEAK_PASSWORD
            is AuthSessionMissingException -> SESSION_EXPIRED
            is AuthRestException -> defaultMessage // wrong credentials, unconfirmed email, etc.
            is UnauthorizedRestException -> SESSION_EXPIRED // expired/invalid JWT on a Postgrest/Storage call
            is HttpRequestException -> NETWORK // supabase-kt couldn't reach the server at all
            is RestException -> defaultMessage // any other Postgrest/Storage/Realtime error —
            // never read .error/.description verbatim, they can contain table/column names.

            // Catches SocketException, UnknownHostException, SSLHandshakeException,
            // ConnectException, FileNotFoundException, etc.
            is kotlinx.io.IOException -> NETWORK

            // SerializationException extends IllegalArgumentException, so it must be
            // listed above the IllegalArgumentException branch below.
            is SerializationException -> PARSE_ERROR

            // NumberFormatException extends IllegalArgumentException — listed
            // separately so the ordering stays explicit if you differentiate it later.
            is NumberFormatException -> defaultMessage

            is IllegalArgumentException, is IllegalStateException -> defaultMessage
            is NullPointerException -> defaultMessage
            is IndexOutOfBoundsException -> defaultMessage
            is ArithmeticException -> defaultMessage
            is ClassCastException -> defaultMessage

            // Last-resort net for anything not explicitly listed (e.g. platform-specific exception types on iOS/JS targets).
            else -> {
                val className = throwable::class.simpleName.orEmpty()
                when {
                    className.contains("Timeout") -> TIMEOUT
                    className.contains("Connect") || className.contains("Host") -> NETWORK
                    else -> defaultMessage
                }
            }
        }
    }

    private fun clientErrorMessage(status: HttpStatusCode, defaultMessage: String): String = when (status.value) {
        400 -> BAD_REQUEST
        401 -> SESSION_EXPIRED
        403 -> PERMISSION_DENIED
        404 -> NOT_FOUND
        408 -> TIMEOUT
        409 -> CONFLICT
        422 -> VALIDATION
        429 -> RATE_LIMITED
        else -> defaultMessage
    }

    private fun serverErrorMessage(status: HttpStatusCode): String = when (status.value) {
        502, 504 -> "Our servers are having trouble connecting. Please try again shortly."
        503 -> MAINTENANCE
        else -> SERVER_BUSY
    }

    /** Sandwich [ApiResponse.Failure.Error] — non-2xx response with a raw body. Body is never read. */
    fun extract(
        failure: ApiResponse.Failure.Error,
        defaultMessage: String = GENERIC
    ): String = defaultMessage

    /** Sandwich [ApiResponse.Failure.Exception] — wraps a real [Throwable]; delegate for full type-specific handling. */
    fun extract(
        failure: ApiResponse.Failure.Exception,
        defaultMessage: String = GENERIC
    ): String = extract(failure.throwable, defaultMessage)

    /** Raw strings (e.g. Store5 Error.Message) have no allow-listed origin — never trusted verbatim. */
    fun extractFromString(
        rawMessage: String,
        defaultMessage: String = GENERIC
    ): String = defaultMessage
}

/**
 * Throw this for a specific, pre-written, UI-safe message instead of the type-based
 * defaults above — e.g. `throw UserFacingException("That email is already registered.")`.
 * This is the ONLY throwable whose message is ever shown verbatim.
 */
class UserFacingException(
    val userMessage: String,
    cause: Throwable? = null
) : Exception(userMessage, cause)