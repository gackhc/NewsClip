package ggack.newsclip.data.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ggack.newsclip.BuildConfig
import ggack.newsclip.data.ArticleModel
import kotlinx.coroutines.CancellationException

class NewsPagingSource(
    private val service: NewsService,
    private val search: String,
    private val apiKey: String = BuildConfig.NYT_API_KEY
) : PagingSource<Int, ArticleModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleModel> {
        val position = params.key ?: 0
        return try {
            check(apiKey.isNotBlank()) { "NYT_API_KEY 설정이 필요합니다." }
            val response = service.articleSearch(apiKey, position, search)
            LoadResult.Page(
                response.response.docs,
                if(position == 0) null else position - 1,
                if(response.response.docs.size < 10 || position >= 99) null else position + 1
            )
        } catch (e: CancellationException) {
            throw e
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
