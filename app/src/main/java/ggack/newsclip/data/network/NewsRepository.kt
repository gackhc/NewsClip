package ggack.newsclip.data.network

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ggack.newsclip.data.models.ArticleModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(private val service : NewsService) {
    fun getSearchResult(): Flow<PagingData<ArticleModel>> {
        return Pager(PagingConfig(10)) {
            NewsPagingSource(service)
        }.flow

    }
}