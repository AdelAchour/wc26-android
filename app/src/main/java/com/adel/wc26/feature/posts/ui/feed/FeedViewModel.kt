package com.adel.wc26.feature.posts.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.adel.wc26.feature.posts.data.PostLikeManager
import com.adel.wc26.feature.posts.data.post.PostPagingSource
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Global feed — all posts across every match, newest first, paged.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postLikeManager: PostLikeManager,
) : ViewModel() {

    // Cache the raw pager stream
    private val pagingFlow = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource { cursor ->
                postRepository.getGlobalFeed(cursor)
            }
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
    fun toggleLike(post: Post) {
        postLikeManager.toggleLike(post, viewModelScope)
    }

}