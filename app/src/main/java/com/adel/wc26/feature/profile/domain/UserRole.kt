package com.adel.wc26.feature.profile.domain

enum class UserRole(val value: String) {
    USER("user"),
    ADMIN("admin");

    companion object {
        fun fromString(value: String): UserRole =
            entries.find { it.value.equals(value, ignoreCase = true) } ?: USER
    }
}