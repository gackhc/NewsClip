package ggack.newsclip.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ProvidedAutoMigrationSpec
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject
import javax.inject.Singleton

data class NewsModel (
    @Json(name = "status") val status : String,
    @Json(name = "response") val response : ResponseModel
)

data class ResponseModel (
    @Json(name = "docs") val docs : List<ArticleModel>,
)

@Entity
data class ArticleModel (
    @Json(name = "headline") val headline : HeadLineModel,
    @Json(name = "web_url") val url : String,
    @PrimaryKey @Json(name = "pub_date") val date : String,
    @Json(name = "multimedia") val multimedia : List<MultimediaModel>
)

data class HeadLineModel (
    @Json(name = "main") val text : String,
)

data class MultimediaModel (
    @Json(name = "url") val url : String,
)

@Singleton
@ProvidedTypeConverter
class Converters @Inject constructor(private val moshi : Moshi) {
    @TypeConverter
    fun fromStringToArticle(str : String) : HeadLineModel? =
        moshi.adapter(HeadLineModel::class.java).fromJson(str)
    @TypeConverter
    fun fromModel(model : HeadLineModel) : String=
        moshi.adapter(HeadLineModel::class.java).toJson(model)
    @TypeConverter
    fun fromStringToMultimedias(str : String) : List<MultimediaModel>?=
        moshi.adapter<List<MultimediaModel>>(Types.newParameterizedType(List::class.java, MultimediaModel::class.java)).fromJson(str)
    @TypeConverter
    fun fromModel(model : List<MultimediaModel>) : String=
        moshi.adapter<List<MultimediaModel>>(Types.newParameterizedType(List::class.java, MultimediaModel::class.java)).toJson(model)
}
