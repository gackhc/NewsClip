package ggack.newsclip.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.data.Converters


@Database(entities = [ArticleModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun clipDao() : ClipDao
}