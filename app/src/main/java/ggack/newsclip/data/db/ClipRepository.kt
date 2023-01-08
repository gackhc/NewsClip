package ggack.newsclip.data.db

import androidx.annotation.WorkerThread
import ggack.newsclip.data.ArticleModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipRepository @Inject constructor(private val clipDao : ClipDao) {
    val clipList : Flow<List<ArticleModel>> = clipDao.getAll()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(article : ArticleModel) {
        clipDao.insert(article)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(article : ArticleModel) {
        clipDao.delete(article)
    }
}