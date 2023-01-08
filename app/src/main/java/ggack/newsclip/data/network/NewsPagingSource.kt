package ggack.newsclip.data.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ggack.newsclip.data.ArticleModel

class NewsPagingSource(private val service : NewsService, private val search : String) : PagingSource<Int, ArticleModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleModel> {
        val position = params.key ?: 1
        return try {
            val response = service.articleSearch("kijN6KuzTKPChAwMTGXnx5GGrgfAX96X", "print_page:$position", search)
            LoadResult.Page(
                response.response.docs,
                if(position == 1) null else position - 1,
                position + 1
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}