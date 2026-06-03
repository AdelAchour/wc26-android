package com.adel.wc26.feature.matches.data

import com.adel.wc26.core.network.dto.PageDto
import com.adel.wc26.feature.matches.data.dto.MatchDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.PATCH

/**
 * Match endpoints. Matches use OFFSET pagination (bounded — 104 rows).
 *
 *
 *   GET /matches?status=&limit=&offset=
 *   GET /matches/{id}
 *
 * The [status] filter is nullable — omitted means all matches.
 * Match the exact accepted status values (e.g. "upcoming", "live",
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

    @PATCH("admin/matches/{id}")
    suspend fun updateMatch(
        @Path("id") id: Long,
        @Body body: MatchUpdateRequest,
    ): MatchDto
}

@Serializable
data class MatchUpdateRequest(
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: String? = null,
)