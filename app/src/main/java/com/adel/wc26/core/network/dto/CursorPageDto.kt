package com.adel.wc26.core.network.dto

import kotlinx.serialization.Serializable

/**
 * Cursor-paginated response envelope — mirrors the backend's CursorPageDto.
 * Used by endpoints with unbounded, infinite-scroll data (posts, likes).
 *
 * When [nextCursor] is null, there are no more pages.
 */
@Serializable
data class CursorPageDto<T>(
    val items: List<T>,
    val nextCursor: String? = null,
)