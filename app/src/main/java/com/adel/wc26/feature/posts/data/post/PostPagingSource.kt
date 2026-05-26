package com.adel.wc26.feature.posts.data.post

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.adel.wc26.core.result.CursorPage
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.domain.post.Post

/**
 * A Paging 3 source over the backend's cursor-paginated post endpoints.
 *
 * The paging "key" is the backend cursor string (a base64 token). The
 * first load uses a null key; each page returns the cursor for the next.
 *
 * [fetch] is supplied by the caller so the same source works for both the
 * global feed and a single match's thread — only the fetch lambda differs.
 */
class PostPagingSource(
    private val fetch: suspend (cursor: String?) -> DataResult<CursorPage<Post>>,
) : PagingSource<String, Post>() {

    override suspend fun load(
        params: LoadParams<String>,
    ): LoadResult<String, Post> {
        val cursor = params.key // null on first load
        return when (val result = fetch(cursor)) {
            is DataResult.Success -> LoadResult.Page(
                data = result.data.items,
                prevKey = null, // forward-only paging
                nextKey = result.data.nextCursor,
            )
            is DataResult.Error -> LoadResult.Error(
                result.cause ?: IllegalStateException(result.error.name),
            )
        }
    }

    /**
     * Paging asks for a refresh key after invalidation. Cursor paging
     * can't easily resume from an arbitrary anchor, so we restart from
     * the top — correct, and fine for a feed.
     */
    override fun getRefreshKey(state: PagingState<String, Post>): String? = null
}