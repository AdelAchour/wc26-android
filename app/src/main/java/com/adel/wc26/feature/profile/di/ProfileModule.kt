package com.adel.wc26.feature.profile.di

import com.adel.wc26.feature.profile.data.UserRepositoryImpl
import com.adel.wc26.feature.profile.domain.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the UserRepository interface to its implementation.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}