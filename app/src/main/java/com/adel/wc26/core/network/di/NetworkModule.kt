package com.adel.wc26.core.network.di

import com.adel.wc26.core.network.ApiConstants
import com.adel.wc26.core.network.AuthInterceptor
import com.adel.wc26.feature.auth.data.AuthApi
import com.adel.wc26.feature.matches.data.MatchApi
import com.adel.wc26.feature.posts.data.comment.CommentApi
import com.adel.wc26.feature.posts.data.like.LikeApi
import com.adel.wc26.feature.posts.data.post.PostApi
import com.adel.wc26.feature.profile.data.UserApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Provides the network stack: JSON serializer, OkHttp client (with the
 * auth interceptor and logging), Retrofit, and every API service.
 *
 * Everything is @Singleton — one shared client and one Retrofit instance
 * for the whole app.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // The backend may add fields over time; don't crash on unknowns.
        ignoreUnknownKeys = true
        // Treat an absent JSON key as the property's default value.
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Full body logging in dev. Tighten/remove for release builds.
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMatchApi(retrofit: Retrofit): MatchApi =
        retrofit.create(MatchApi::class.java)

    @Provides
    @Singleton
    fun providePostApi(retrofit: Retrofit): PostApi =
        retrofit.create(PostApi::class.java)

    @Provides
    @Singleton
    fun provideCommentApi(retrofit: Retrofit): CommentApi =
        retrofit.create(CommentApi::class.java)

    @Provides
    @Singleton
    fun provideLikeApi(retrofit: Retrofit): LikeApi =
        retrofit.create(LikeApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)
}