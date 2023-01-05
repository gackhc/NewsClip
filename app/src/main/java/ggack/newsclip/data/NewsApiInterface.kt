package ggack.newsclip.data

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiInterface {
    @GET("articlesearch.json")
    suspend fun articleSearch(@Query("api-key") key : String,  @Query("fq") filter : String? = null, @Query("q") query : String? = null, ) : NewsModel
}