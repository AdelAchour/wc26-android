package com.adel.wc26.core.network.dto

import kotlinx.serialization.Serializable

/**
 * Offset-paginated response envelope — mirrors the backend's PageDto.
 * Used by endpoints with bounded data where total count is meaningful
 * (matches, comments).
 */
@Serializable
data class PageDto<T>(
    val items: List<T>,
    val total: Long,
    val limit: Int,
    val offset: Long,
)