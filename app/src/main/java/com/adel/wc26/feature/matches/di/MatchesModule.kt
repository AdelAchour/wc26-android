package com.adel.wc26.feature.matches.di

import com.adel.wc26.feature.matches.data.MatchRepositoryImpl
import com.adel.wc26.feature.matches.domain.MatchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the MatchRepository interface to its implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MatchesModule {

    @Binds
    @Singleton
    abstract fun bindMatchRepository(impl: MatchRepositoryImpl): MatchRepository
}