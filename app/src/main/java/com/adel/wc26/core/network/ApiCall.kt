package com.adel.wc26.core.network

import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.auth.data.dto.ErrorResponse
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs a network call and wraps the outcome in a [DataResult], translating
 * exceptions into a semantic [AppError] — never a user-facing string.
 *
 * - HttpException  -> mapped by status code
 * - IOException    -> no connection / timeout
 * - anything else  -> generic fallback
 *
 * Repositories call this so they never leak raw exceptions to ViewModels,
 * and so no Android resources are needed in the data layer.
 */
suspend fun <T> apiCall(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(data = block())
    } catch (e: HttpException) {
        DataResult.Error(error = httpError(code = e.code()), cause = e)
    } catch (e: IOException) {
        DataResult.Error(error = AppError.Network, cause = e)
    } catch (e: Exception) {
        DataResult.Error(error = AppError.Unknown, cause = e)
    }

/**
 * Like [apiCall], but on an HTTP error it attempts to parse the server's
 * JSON error body for a specific message (e.g. `{"error": "Invalid or expired reset code"}`).
 * If parsing succeeds, returns [AppError.ServerMessage]; otherwise falls back
 * to the standard status-code mapping.
 */
suspend fun <T> apiCallWithServerMessage(
    json: Json,
    block: suspend () -> T,
): DataResult<T> =
    try {
        DataResult.Success(data = block())
    } catch (e: HttpException) {
        val serverMsg = try {
            e.response()?.errorBody()?.string()?.let { body ->
                json.decodeFromString<ErrorResponse>(body).error
            }
        } catch (_: Exception) { null }
        DataResult.Error(
            error = serverMsg?.let { AppError.ServerMessage(it) } ?: httpError(e.code()),
            cause = e,
        )
    } catch (e: IOException) {
        DataResult.Error(error = AppError.Network, cause = e)
    } catch (e: Exception) {
        DataResult.Error(error = AppError.Unknown, cause = e)
    }

private fun httpError(code: Int): AppError = when (code) {
    400 -> AppError.BadRequest
    401 -> AppError.Unauthorized
    403 -> AppError.Forbidden
    404 -> AppError.NotFound
    409 -> AppError.Conflict
    in 500..599 -> AppError.Server
    else -> AppError.Unknown
}