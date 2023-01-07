package ggack.newsclip.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ggack.newsclip.data.models.ArticleModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Query("SELECT * FROM ArticleModel")
    fun getAll() : Flow<List<ArticleModel>>

    @Query("SELECT * FROM ArticleModel WHERE date IS :date")
    suspend fun getByDate(date : String) : ArticleModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article : ArticleModel)

    @Delete
    suspend fun delete(article : ArticleModel)
}