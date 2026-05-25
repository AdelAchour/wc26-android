package com.adel.wc26.feature.profile.data

import com.adel.wc26.feature.profile.data.dto.UserDto
import com.adel.wc26.feature.profile.data.dto.UserPublicDto
import com.adel.wc26.feature.profile.domain.PublicProfile
import com.adel.wc26.feature.profile.domain.UserProfile

/**
 * Mappers: wire DTOs -> domain models.
 * The data layer owns this translation so the UI never sees a raw DTO.
 */

fun UserDto.toDomain(): UserProfile = UserProfile(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    role = role,
    joinedAt = createdAt,
)

fun UserPublicDto.toDomain(): PublicProfile = PublicProfile(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
    joinedAt = createdAt,
)