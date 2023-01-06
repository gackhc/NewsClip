package ggack.newsclip.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ggack.newsclip.data.models.ArticleModel

@Dao
interface ClipDao {
    @Query("SELECT * FROM ArticleModel")
    fun getAll() : List<ArticleModel>

    @Query("SELECT * FROM ArticleModel WHERE date IS :date")
    fun getByDate(date : String) : ArticleModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(article : ArticleModel)

    @Delete
    fun delete(article : ArticleModel)
}