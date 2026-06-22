package com.adel.wc26.core.ui

import android.content.Context
import androidx.annotation.StringRes
import com.adel.wc26.R
import com.adel.wc26.core.result.AppError

/**
 * Maps a semantic [AppError] to a localized string resource.
 *
 * This is the bridge between the resource-free data/domain layers and
 * the UI: the ViewModel holds an AppError, the Composable resolves it
 * with stringResource(error.toStringRes()).
 */
@StringRes
fun AppError.toStringRes(): Int = when (this) {
    AppError.Network -> R.string.error_network
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.Forbidden -> R.string.error_forbidden
    AppError.NotFound -> R.string.error_not_found
    AppError.Conflict -> R.string.error_conflict
    AppError.BadRequest -> R.string.error_bad_request
    AppError.Server -> R.string.error_server
    AppError.Unknown -> R.string.error_unknown
    is AppError.ServerMessage -> R.string.error_unknown
}

/**
 * Returns a display-ready error string.
 *
 * For [AppError.ServerMessage] returns the raw server message directly.
 * For all other errors, resolves to a localized string resource.
 */
fun AppError.toDisplayString(context: Context): String = when (this) {
    is AppError.ServerMessage -> message
    else -> context.getString(toStringRes())
}