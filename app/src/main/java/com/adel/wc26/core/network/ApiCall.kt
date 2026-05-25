package com.adel.wc26.core.network

import com.adel.wc26.core.result.DataResult
import retrofit2.HttpException
import java.io.IOException

/**
 * Runs a network call and wraps the outcome in a [DataResult], translating
 * exceptions into user-presentable messages.
 *
 * - HttpException  → mapped by status code
 * - IOException    → no connection / timeout
 * - anything else  → generic fallback
 *
 * Repositories call this so they never leak raw exceptions to ViewModels.
 */
suspend fun <T> apiCall(block: suspend () -> T): DataResult<T> =
    try {
        DataResult.Success(block())
    } catch (e: HttpException) {
        DataResult.Error(httpMessage(e.code()), e)
    } catch (e: IOException) {
        DataResult.Error("No connection. Check your network and try again.", e)
    } catch (e: Exception) {
        DataResult.Error("Something went wrong. Please try again.", e)
    }

private fun httpMessage(code: Int): String = when (code) {
    400 -> "That request wasn't valid."
    401 -> "Incorrect email or password."
    403 -> "You don't have permission to do that."
    404 -> "Not found."
    409 -> "That's already taken."
    in 500..599 -> "The server had a problem. Try again shortly."
    else -> "Request failed ($code)."
}