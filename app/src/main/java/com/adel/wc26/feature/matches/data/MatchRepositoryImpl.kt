package com.adel.wc26.feature.matches.data

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.matches.domain.MatchFilter
import com.adel.wc26.feature.matches.domain.MatchRepository
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MatchRepository implementation.
 *
 * The matches dataset is bounded (104 rows), so this fetches with a high
 * limit and hands back the whole list — no paging needed. The backend
 * uses offset pagination here; we request a single large page.
 */
@Singleton
class MatchRepositoryImpl @Inject constructor(
    private val matchApi: MatchApi,
    private val matchUpdateNotifier: MatchUpdateNotifier,
) : MatchRepository {

    private companion object {
        // Comfortably above the 104-match tournament total.
        const val PAGE_LIMIT = 200
    }

    override suspend fun getMatches(filter: MatchFilter): DataResult<List<Match>> =
        apiCall {
            matchApi.getMatches(
                status = filter.apiValue,
                limit = PAGE_LIMIT,
                offset = 0,
            )
        }.map { page ->
            page.items.map { it.toDomain() }
        }

    override suspend fun getMatch(id: Long): DataResult<Match> =
        apiCall { matchApi.getMatch(id) }.map { it.toDomain() }

    override suspend fun updateMatch(
        id: Long,
        homeScore: Int?,
        awayScore: Int?,
        status: MatchStatus?,
        homeTeam: String?,
        awayTeam: String?,
    ): DataResult<Match> =
        apiCall {
            matchApi.updateMatch(
                id = id,
                body = MatchUpdateRequest(
                    homeScore = homeScore,
                    awayScore = awayScore,
                    status = status?.value,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                )
            )
        }.map { dto ->
            val domainMatch = dto.toDomain()
            // Notify screens of the update live!
            matchUpdateNotifier.notifyMatchUpdated(domainMatch)
            domainMatch
        }
}