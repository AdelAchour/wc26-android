package com.adel.wc26.core.result

/**
 * A simple success/failure result for repository operations.
 *
 * Repositories return this instead of throwing — callers handle both
 * branches explicitly with a `when`. [Error] carries a semantic [AppError]
 * (not a string); the UI layer resolves it to localized text. The cause
 * is kept for logging.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(
        val error: AppError,
        val cause: Throwable? = null,
    ) : DataResult<Nothing>
}

/** Convenience for mapping the success value. */
inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> =
    when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Error -> this
    }