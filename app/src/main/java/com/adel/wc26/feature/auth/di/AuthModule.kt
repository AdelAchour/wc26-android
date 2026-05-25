package com.adel.wc26.feature.auth.di

import com.adel.wc26.feature.auth.data.AuthRepositoryImpl
import com.adel.wc26.feature.auth.domain.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the AuthRepository interface to its implementation.
 *
 * @Binds (rather than @Provides) is the lean way to wire an
 * interface→impl when the impl has an @Inject constructor — no
 * function body needed.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}