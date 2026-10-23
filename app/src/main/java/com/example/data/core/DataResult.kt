package com.example.data.core

import androidx.annotation.StringRes
import com.example.R
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * The single result type used by every repository in SiraHub.
 *
 * Repositories must never signal failure by returning an empty list or a bare null. An empty
 * [Success] means "the server said there is nothing", which is a different fact from "the request
 * failed". Screens can therefore render an honest empty state instead of an error, and vice versa.
 */
sealed interface DataResult<out T> {

    data class Success<T>(val data: T) : DataResult<T>

    /** The device could not reach Supabase (no connectivity, DNS, timeout, socket failure). */
    data class NetworkError(val cause: Throwable? = null) : DataResult<Nothing>

    /** The session is missing, expired, or rejected by the server (HTTP 401 / 403). */
    data class AuthError(val cause: Throwable? = null) : DataResult<Nothing>

    /** The request was malformed or rejected by a database constraint / RLS policy (HTTP 400 / 422). */
    data class ValidationError(
        @StringRes val messageRes: Int = R.string.error_validation_failed,
        val cause: Throwable? = null
    ) : DataResult<Nothing>

    /** The server failed or returned something we could not understand (HTTP 5xx, bad payload). */
    data class ServerError(val cause: Throwable? = null) : DataResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success

    fun dataOrNull(): T? = (this as? Success)?.data

    fun <R> map(transform: (T) -> R): DataResult<R> = when (this) {
        is Success -> Success(transform(data))
        is NetworkError -> this
        is AuthError -> this
        is ValidationError -> this
        is ServerError -> this
    }
}

/**
 * Runs [block] and converts any thrown exception into the matching [DataResult] failure.
 *
 * This exists so that no repository needs a bare `catch (e: Exception) { return emptyList() }`.
 * Every failure is classified, logged by the caller, and surfaced to the user.
 */
inline fun <T> dataResultOf(block: () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: Throwable) {
        e.toDataResultFailure()
    }

/** Classifies a throwable into the appropriate [DataResult] failure variant. */
fun Throwable.toDataResultFailure(): DataResult<Nothing> = when (this) {
    is UnknownHostException,
    is ConnectException,
    is SocketTimeoutException,
    is NoRouteToHostException,
    is InterruptedIOException,
    is SSLException -> DataResult.NetworkError(this)

    else -> DataResult.ServerError(this)
}
