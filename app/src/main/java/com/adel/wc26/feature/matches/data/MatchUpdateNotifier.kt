package com.adel.wc26.feature.matches.data

import com.adel.wc26.feature.matches.domain.model.Match
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchUpdateNotifier @Inject constructor() {
    private val _matchUpdated = MutableSharedFlow<Match>(extraBufferCapacity = 1)
    val matchUpdated = _matchUpdated.asSharedFlow()

    fun notifyMatchUpdated(match: Match) {
        _matchUpdated.tryEmit(match)
    }
}