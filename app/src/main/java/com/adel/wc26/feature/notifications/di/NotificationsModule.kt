package com.adel.wc26.feature.notifications.di

import com.adel.wc26.feature.notifications.data.NotificationsApi
import com.adel.wc26.feature.notifications.data.NotificationsRepositoryImpl
import com.adel.wc26.feature.notifications.domain.NotificationsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(
        impl: NotificationsRepositoryImpl,
    ): NotificationsRepository

    companion object {
        @Provides
        @Singleton
        fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi =
            retrofit.create(NotificationsApi::class.java)
    }
}