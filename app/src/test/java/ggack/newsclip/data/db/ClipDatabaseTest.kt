package ggack.newsclip.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.data.Converters
import ggack.newsclip.data.HeadLineModel
import ggack.newsclip.data.MultimediaModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClipDatabaseTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var db: AppDataBase
    private val first = ArticleModel(HeadLineModel("First"), "https://example.test/first",
        "2023-01-01T00:00:00+0000", listOf(MultimediaModel("images/first.jpg")))
    private val second = first.copy(headline = HeadLineModel("Second"), url = "https://example.test/second")

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            .addTypeConverter(Converters()).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        db.close()

    }

    @Test
    fun differentArticlesAtSamePublicationTimeArePreserved() = runBlocking {
        db.clipDao().insert(first)
        db.clipDao().insert(second)
        assertEquals(setOf(first, second), db.clipDao().getAll().first().toSet())
        db.clipDao().delete(first)
        assertEquals(listOf(second), db.clipDao().getAll().first())
    }

    @Test
    fun savingSameUrlUpdatesArticleWithoutDuplicates() = runBlocking {
        db.clipDao().insert(first)
        val updated = first.copy(headline = HeadLineModel("Updated"), date = "2023-01-02T00:00:00+0000")
        db.clipDao().insert(updated)
        assertEquals(listOf(updated), db.clipDao().getAll().first())
    }

}
