package com.adel.wc26.core.result

/**
 * A semantic description of what went wrong — NOT a user-facing string.
 *
 * The data and domain layers produce these; the UI layer maps them to
 * localized text via stringResource(). This keeps data/domain free of
 * Android resources and Context, and keeps everything localizable.
 */
enum class AppError {
    Network,        // no connection / timeout
    Unauthorized,   // 401
    Forbidden,      // 403
    NotFound,       // 404
    Conflict,       // 409 — already taken
    BadRequest,     // 400
    Server,         // 5xx
    Unknown,
}