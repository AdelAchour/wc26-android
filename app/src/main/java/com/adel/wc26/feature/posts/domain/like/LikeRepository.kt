package com.adel.wc26.feature.posts.domain.like

import com.adel.wc26.core.result.DataResult

/**
 * Like data. Like / unlike are idempotent on the backend, so callers can
 * fire them without checking current state first.
 */
interface LikeRepository {

    /** Like a post (idempotent). */
    suspend fun like(postId: Long): DataResult<Unit>

    /** Unlike a post (idempotent). */
    suspend fun unlike(postId: Long): DataResult<Unit>
}