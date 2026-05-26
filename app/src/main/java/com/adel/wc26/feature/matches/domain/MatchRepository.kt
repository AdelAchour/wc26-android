package com.adel.wc26.feature.matches.domain

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.matches.domain.model.Match

/**
 * The filter the matches list can apply. ALL means no status filter.
 * Maps to the backend's ?status= query param (null for ALL).
 */
enum class MatchFilter(val apiValue: String?) {
    ALL(null),
    UPCOMING("upcoming"),
    LIVE("live"),
    FINISHED("finished"),
}

/**
 * Matches data. The list is bounded (104 matches), so this uses simple
 * bounded fetches rather than paging.
 */
interface MatchRepository {

    /**
     * Fetch matches, optionally filtered by status.
     * Returns the full set for the filter (the dataset is small enough
     * that the app can hold all of them).
     */
    suspend fun getMatches(filter: MatchFilter): DataResult<List<Match>>

    /** Fetch a single match by id. */
    suspend fun getMatch(id: Long): DataResult<Match>
}