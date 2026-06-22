package com.adel.wc26.core.result

/**
 * A semantic description of what went wrong — NOT a user-facing string.
 *
 * The data and domain layers produce these; the UI layer maps them to
 * localized text via stringResource(). This keeps data/domain free of
 * Android resources and Context, and keeps everything localizable.
 */
sealed interface AppError {
    data object Network : AppError        // no connection / timeout
    data object Unauthorized : AppError   // 401
    data object Forbidden : AppError      // 403
    data object NotFound : AppError       // 404
    data object Conflict : AppError       // 409 — already taken
    data object BadRequest : AppError     // 400
    data object Server : AppError         // 5xx
    data object Unknown : AppError
    /** Carries the raw error message from the server response body. */
    data class ServerMessage(val message: String) : AppError
}