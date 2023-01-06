package ggack.newsclip.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ggack.newsclip.data.models.ArticleModel


@Database(entities = [ArticleModel::class], version = 1)
abstract class AppDataBase : RoomDatabase() {
}