package com.adel.wc26.core.result

/**
 * A page of cursor-paginated results, in domain terms.
 * [nextCursor] is null when there are no more pages.
 *
 * Mirrors the backend's CursorPageDto but lives in the domain layer so
 * repositories can return it without exposing wire DTOs.
 */
data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: String?,
)