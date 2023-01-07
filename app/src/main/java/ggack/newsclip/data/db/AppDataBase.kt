package ggack.newsclip.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ggack.newsclip.data.models.ArticleModel
import ggack.newsclip.data.models.Converters
import ggack.newsclip.data.models.HeadLineModel
import ggack.newsclip.data.models.MultimediaModel


@Database(entities = [ArticleModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun clipDao() : ClipDao
}