package ggack.newsclip.data

import com.squareup.moshi.Json

data class NewsModel (
    @Json(name = "status") val status : String?,
    @Json(name = "response") val response : ResponseModel?
)

data class ResponseModel (
    @Json(name = "docs") val docs : List<ArticleModel>?,
)

data class ArticleModel (
    @Json(name = "headline") val headline : HeadLineModel?,
    @Json(name = "web_url") val url : String?,
    @Json(name = "pub_date") val date : String?,
    @Json(name = "multimedia") val multimedia : List<MultimediaModel>?
)

data class HeadLineModel (
    @Json(name = "main") val text : String?,
)
data class MultimediaModel (
    @Json(name = "url") val url : String?,
)