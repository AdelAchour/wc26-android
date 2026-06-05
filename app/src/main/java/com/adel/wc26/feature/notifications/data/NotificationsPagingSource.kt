package com.adel.wc26.feature.notifications.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.notifications.domain.Notification

class NotificationsPagingSource(
    private val fetch: suspend (limit: Int, offset: Long) -> DataResult<List<Notification>>,
) : PagingSource<Int, Notification>() {

    override suspend fun load(
        params: LoadParams<Int>,
    ): LoadResult<Int, Notification> {
        val offset = params.key ?: 0
        val limit = params.loadSize

        return when (val result = fetch(limit, offset.toLong())) {
            is DataResult.Success -> {
                val items = result.data
                LoadResult.Page(
                    data = items,
                    prevKey = if (offset == 0) null else maxOf(0, offset - limit),
                    nextKey = if (items.size < limit) null else offset + items.size,
                )
            }
            is DataResult.Error -> {
                LoadResult.Error(
                    result.cause ?: IllegalStateException(result.error.name)
                )
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Notification>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.let { it + state.config.pageSize }
                ?: 0
        }
    }
}