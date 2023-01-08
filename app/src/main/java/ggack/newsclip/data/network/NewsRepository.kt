package ggack.newsclip.data.network

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ggack.newsclip.data.ArticleModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(private val service : NewsService) {
    fun getSearchResult(search : String): Flow<PagingData<ArticleModel>> {
        return Pager(PagingConfig(10)) {
            NewsPagingSource(service, search)
        }.flow
    }
}