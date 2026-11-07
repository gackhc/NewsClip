package ggack.newsclip.data.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.data.HeadLineModel
import ggack.newsclip.data.MultimediaModel

// Keep the API's changing image envelope separate from the persisted article format.
class ArticleJsonAdapter {
    @FromJson
    fun fromJson(article: ApiArticle): ArticleModel {
        val images = when (val media = article.multimedia) {
            is List<*> -> media
            is Map<*, *> -> listOfNotNull(media["default"], media["thumbnail"])
            else -> emptyList<Any>()
        }.mapNotNull { (it as? Map<*, *>)?.get("url") as? String }
            .filter { it.isNotBlank() }.distinct().map { MultimediaModel(it) }
        return ArticleModel(article.headline, article.url, article.date, images)
    }
}

data class ApiArticle(
    val headline: HeadLineModel,
    @Json(name = "web_url") val url: String,
    @Json(name = "pub_date") val date: String,
    val multimedia: Any? = null
)
