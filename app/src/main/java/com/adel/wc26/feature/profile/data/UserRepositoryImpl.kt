package com.adel.wc26.feature.profile.data

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.CursorPage
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.auth.data.AuthApi
import com.adel.wc26.feature.posts.data.toDomain
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.profile.domain.PublicProfile
import com.adel.wc26.feature.profile.domain.UserProfile
import com.adel.wc26.feature.profile.domain.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserRepository implementation.
 *
 * The signed-in user's own profile comes from AuthApi.me() (GET /auth/me) —
 * that endpoint already returns the full private UserDto. Public lookups,
 * and a user's posts / liked posts, go through UserApi.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userApi: UserApi,
) : UserRepository {

    private companion object {
        const val PAGE_SIZE = 20
    }

    override suspend fun getMyProfile(): DataResult<UserProfile> =
        apiCall { authApi.me() }.map { it.toDomain() }

    override suspend fun getPublicProfile(userId: Long): DataResult<PublicProfile> =
        apiCall { userApi.getUser(userId) }.map { it.toDomain() }

    override suspend fun getUserPosts(
        userId: Long,
        cursor: String?,
    ): DataResult<CursorPage<Post>> =
        apiCall { userApi.getUserPosts(userId, cursor, PAGE_SIZE) }
            .map { dto ->
                CursorPage(
                    items = dto.items.map { it.toDomain() },
                    nextCursor = dto.nextCursor,
                )
            }

    override suspend fun getUserLikes(
        userId: Long,
        cursor: String?,
    ): DataResult<CursorPage<Post>> =
        apiCall { userApi.getUserLikes(userId, cursor, PAGE_SIZE) }
            .map { dto ->
                CursorPage(
                    items = dto.items.map { it.toDomain() },
                    nextCursor = dto.nextCursor,
                )
            }
}