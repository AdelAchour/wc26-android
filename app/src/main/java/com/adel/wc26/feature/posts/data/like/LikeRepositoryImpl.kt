package com.adel.wc26.feature.posts.data.like

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.domain.like.LikeRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LikeRepository implementation — talks to LikeApi.
 * Like / unlike are idempotent on the backend.
 */
@Singleton
class LikeRepositoryImpl @Inject constructor(
    private val likeApi: LikeApi,
) : LikeRepository {

    override suspend fun like(postId: Long): DataResult<Unit> =
        apiCall { likeApi.like(postId) }

    override suspend fun unlike(postId: Long): DataResult<Unit> =
        apiCall { likeApi.unlike(postId) }
}