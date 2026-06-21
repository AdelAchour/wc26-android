package com.adel.wc26.feature.predictions.data

import com.adel.wc26.feature.predictions.domain.model.Prediction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Broadcasts a freshly saved prediction so every screen showing that match
 * (list, detail) updates its embedded prediction live — mirrors MatchUpdateNotifier.
 */
@Singleton
class PredictionUpdateNotifier @Inject constructor() {
    private val _predictionUpdated = MutableSharedFlow<Prediction>(extraBufferCapacity = 1)
    val predictionUpdated = _predictionUpdated.asSharedFlow()

    fun notifyPredictionUpdated(prediction: Prediction) {
        _predictionUpdated.tryEmit(prediction)
    }
}
