package com.adel.wc26.core.ui

import androidx.annotation.StringRes
import com.adel.wc26.R
import com.adel.wc26.feature.auth.ui.ValidationError

/**
 * Maps a [ValidationError] to a localized string resource.
 * Resolved in the UI layer with stringResource().
 */
@StringRes
fun ValidationError.toStringRes(): Int = when (this) {
    ValidationError.EmailRequired -> R.string.validation_email_required
    ValidationError.EmailInvalid -> R.string.validation_email_invalid
    ValidationError.UsernameRequired -> R.string.validation_username_required
    ValidationError.UsernameFormat -> R.string.validation_username_format
    ValidationError.PasswordRequired -> R.string.validation_password_required
    ValidationError.PasswordTooShort -> R.string.validation_password_too_short
    ValidationError.DisplayNameRequired -> R.string.validation_display_name_required
    ValidationError.DisplayNameTooLong -> R.string.validation_display_name_too_long
}