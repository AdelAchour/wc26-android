package com.adel.wc26.feature.posts.domain.post

import com.adel.wc26.core.result.CursorPage
import com.adel.wc26.core.result.DataResult

/**
 * Post data. Feeds are cursor-paginated; a null cursor means "first page".
 *
 * The Paging 3 PagingSource calls [getMatchPosts] / [getGlobalFeed] page
 * by page; single-shot screens can call them directly too.
 */
interface PostRepository {

    /** A page of the global feed (all posts, newest first). */
    suspend fun getGlobalFeed(cursor: String?): DataResult<CursorPage<Post>>

    /** A page of one match's post thread. */
    suspend fun getMatchPosts(matchId: Long, cursor: String?): DataResult<CursorPage<Post>>

    /** A single post by id. */
    suspend fun getPost(postId: Long): DataResult<Post>

    /** Create a post under a match. Returns the created post. */
    suspend fun createPost(matchId: Long, content: String): DataResult<Post>

    /** Delete a post. */
    suspend fun deletePost(postId: Long): DataResult<Unit>
}