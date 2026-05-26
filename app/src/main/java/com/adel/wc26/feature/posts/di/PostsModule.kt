package com.adel.wc26.feature.posts.di

import com.adel.wc26.feature.posts.data.comment.CommentRepositoryImpl
import com.adel.wc26.feature.posts.data.like.LikeRepositoryImpl
import com.adel.wc26.feature.posts.data.post.PostRepositoryImpl
import com.adel.wc26.feature.posts.domain.comment.CommentRepository
import com.adel.wc26.feature.posts.domain.like.LikeRepository
import com.adel.wc26.feature.posts.domain.post.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the posts-feature repository interfaces to their implementations:
 * posts, comments, and likes.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PostsModule {

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun bindLikeRepository(impl: LikeRepositoryImpl): LikeRepository
}