package com.adel.wc26.feature.predictions.di

import com.adel.wc26.feature.predictions.data.PredictionRepositoryImpl
import com.adel.wc26.feature.predictions.domain.PredictionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PredictionsModule {

    @Binds
    @Singleton
    abstract fun bindPredictionRepository(impl: PredictionRepositoryImpl): PredictionRepository
}
