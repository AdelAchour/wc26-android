package com.adel.wc26.feature.auth.ui

/**
 * Client-side form validation for the auth screens.
 *
 * These rules MIRROR the backend's validation so the user gets instant
 * feedback instead of a network round-trip. The backend remains the
 * source of truth — this is a UX convenience, not a security boundary.
 *
 * Backend rules being mirrored:
 *   username  ^[a-z][a-z0-9_]{2,19}$  (lowercase start, 3-20 chars total)
 *   password  minimum 8 characters
 *   email     basic shape check
 */
object AuthValidation {

    private val USERNAME_REGEX = Regex("^[a-z][a-z0-9_]{2,19}$")
    private const val MIN_PASSWORD_LENGTH = 8
    private const val MAX_DISPLAY_NAME = 50

    /** Returns an error message, or null if valid. */
    fun emailError(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !email.contains("@") || !email.contains(".") ->
            "Enter a valid email address"
        else -> null
    }

    fun usernameError(username: String): String? = when {
        username.isBlank() -> "Username is required"
        !USERNAME_REGEX.matches(username) ->
            "3–20 chars, lowercase letters, digits and underscores; must start with a letter"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isBlank() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH ->
            "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    fun displayNameError(displayName: String): String? = when {
        displayName.isBlank() -> "Display name is required"
        displayName.length > MAX_DISPLAY_NAME ->
            "Display name is too long"
        else -> null
    }
}