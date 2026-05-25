package com.adel.wc26.feature.matches.data

import com.adel.wc26.core.network.dto.PageDto
import com.adel.wc26.feature.matches.data.dto.MatchDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Match endpoints. Matches use OFFSET pagination (bounded — 104 rows).
 *
 *
 *   GET /matches?status=&limit=&offset=
 *   GET /matches/{id}
 *
 * The [status] filter is nullable — omitted means all matches.
 * Confirm the exact accepted status values (e.g. "upcoming", "live",
 * "finished") with the backend's MatchStatus enum.
 */
interface MatchApi {

    @GET("matches")
    suspend fun getMatches(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Long = 0,
    ): PageDto<MatchDto>

    @GET("matches/{id}")
    suspend fun getMatch(@Path("id") id: Long): MatchDto
}