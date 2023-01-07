package ggack.newsclip.data.network

import ggack.newsclip.data.models.NewsModel
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {
    @GET("articlesearch.json")
    suspend fun articleSearch(@Query("api-key") key : String,  @Query("fq") filter : String? = null, @Query("q") query : String? = null, ) : NewsModel
}