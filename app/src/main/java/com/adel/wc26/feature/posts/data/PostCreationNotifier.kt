package com.adel.wc26.feature.posts.data


import com.adel.wc26.feature.posts.domain.post.Post
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostCreationNotifier @Inject constructor() {
    private val _postCreated = MutableSharedFlow<Post>(extraBufferCapacity = 1)
    val postCreated = _postCreated.asSharedFlow()

    fun notifyPostCreated(post: Post) {
        _postCreated.tryEmit(post)
    }
}