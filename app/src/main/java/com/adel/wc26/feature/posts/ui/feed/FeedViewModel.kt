package com.adel.wc26.feature.posts.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.feature.posts.data.PostCreationNotifier
import com.adel.wc26.feature.posts.data.PostDeletionManager
import com.adel.wc26.feature.posts.data.PostLikeManager
import com.adel.wc26.feature.posts.data.post.PostPagingSource
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Global feed — all posts across every match, newest first, paged.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postLikeManager: PostLikeManager,
    private val postDeletionManager: PostDeletionManager,
    private val postCreationNotifier: PostCreationNotifier,
    private val tokenStore: TokenStore,
) : ViewModel() {

    // Expose the current user's ID for showing delete actions
    val currentUserId: Flow<Long?> = flow {
        emit(tokenStore.getUserId())
    }

    // Keep track of the active paging source to invalidate it
    private var currentPagingSource: PostPagingSource? = null

    // Cache the raw pager stream
    private val pagingFlow = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            val source = PostPagingSource { cursor ->
                postRepository.getGlobalFeed(cursor)
            }
            currentPagingSource = source
            source
        },
    ).flow.cachedIn(viewModelScope)

    // Combine with liked states flow so changes recompose dynamically in-place
    val posts: Flow<PagingData<Post>> = pagingFlow
        .combine(postLikeManager.likedStates) { pagingData, likedStates ->
            pagingData.map { post ->
                likedStates[post.id]?.let { status ->
                    post.copy(
                        likedByCurrentUser = status.likedByCurrentUser,
                        likeCount = status.likeCount
                    )
                } ?: post
            }
        }
        .combine(postDeletionManager.deletedPostIds) { pagingData, deletedIds ->
            // Filter out deleted posts dynamically
            pagingData.filter { post -> post.id !in deletedIds }
        }

    init {
        viewModelScope.launch {
            postCreationNotifier.postCreated.collect {
                currentPagingSource?.invalidate()
            }
        }
    }


    fun toggleLike(post: Post) {
        postLikeManager.toggleLike(post, viewModelScope)
    }

    fun deletePost(post: Post, onFailure: () -> Unit = {}) {
        postDeletionManager.deletePost(post.id, viewModelScope, onFailure)
    }
}