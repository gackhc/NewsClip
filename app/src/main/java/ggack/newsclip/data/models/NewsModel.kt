package ggack.newsclip.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

data class NewsModel (
    @Json(name = "status") val status : String?,
    @Json(name = "response") val response : ResponseModel?
)

data class ResponseModel (
    @Json(name = "docs") val docs : List<ArticleModel>?,
)

@Entity
data class ArticleModel (
    @ColumnInfo(name = "headline") @Json(name = "headline") val headline : HeadLineModel?,
    @ColumnInfo(name = "web_url") @Json(name = "web_url") val url : String?,
    @PrimaryKey @Json(name = "pub_date") val date : String?,
    @ColumnInfo(name = "multimedia") @Json(name = "multimedia") val multimedia : List<MultimediaModel>?
)

data class HeadLineModel (
    @Json(name = "main") val text : String?,
)
data class MultimediaModel (
    @Json(name = "url") val url : String?,
)