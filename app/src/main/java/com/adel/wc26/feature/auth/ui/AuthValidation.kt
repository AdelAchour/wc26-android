package com.adel.wc26.feature.auth.ui

/**
 * Identifies which validation rule failed — NOT a user-facing string.
 * The UI maps these to localized text via stringResource().
 */
enum class ValidationError {
    EmailRequired, EmailInvalid,
    UsernameRequired, UsernameFormat,
    PasswordRequired, PasswordTooShort,
    DisplayNameRequired, DisplayNameTooLong,
}

/**
 * Client-side form validation for the auth screens.
 *
 * These rules MIRROR the backend's validation so the user gets instant
 * feedback instead of a network round-trip. The backend remains the
 * source of truth — this is a UX convenience, not a security boundary.
 *
 * Each function returns a [ValidationError] identifying the failed rule,
 * or null when the value is valid.
 *
 * Backend rules being mirrored:
 *   username  ^[a-z][a-z0-9_]{2,19}$  (lowercase start, 3-20 chars total)
 *   password  minimum 8 characters
 */
object AuthValidation {

    private val USERNAME_REGEX = Regex("^[a-z][a-z0-9_]{2,19}$")
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_DISPLAY_NAME = 50

    fun emailError(email: String): ValidationError? = when {
        email.isBlank() -> ValidationError.EmailRequired
        !email.contains("@") || !email.contains(".") -> ValidationError.EmailInvalid
        else -> null
    }

    fun usernameError(username: String): ValidationError? = when {
        username.isBlank() -> ValidationError.UsernameRequired
        !USERNAME_REGEX.matches(username) -> ValidationError.UsernameFormat
        else -> null
    }

    fun passwordError(password: String): ValidationError? = when {
        password.isBlank() -> ValidationError.PasswordRequired
        password.length < MIN_PASSWORD_LENGTH -> ValidationError.PasswordTooShort
        else -> null
    }

    fun displayNameError(displayName: String): ValidationError? = when {
        displayName.isBlank() -> ValidationError.DisplayNameRequired
        displayName.length > MAX_DISPLAY_NAME -> ValidationError.DisplayNameTooLong
        else -> null
    }
}