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
        context.deleteDatabase("clip_database")
    }

    @Test
    fun differentArticlesAtSamePublicationTimeArePreserved() = runBlocking {
        db.clipDao().insert(first)
        db.clipDao().insert(second)
        assertEquals(setOf(first, second), db.clipDao().getAll().first().toSet())
        db.clipDao().delete(first)
        assertEquals(listOf(second), db.clipDao().getAll().first())
        assertNull(db.clipDao().getByUrl(first.url))
    }

    @Test
    fun savingSameUrlUpdatesArticleWithoutDuplicates() = runBlocking {
        db.clipDao().insert(first)
        val updated = first.copy(headline = HeadLineModel("Updated"), date = "2023-01-02T00:00:00+0000")
        db.clipDao().insert(updated)
        assertEquals(listOf(updated), db.clipDao().getAll().first())
        assertEquals(updated, db.clipDao().getByUrl(first.url))
    }

    @Test
    fun productionMigrationPreservesClipsAndUsesUrlAsPrimaryKey() = runBlocking {
        val oldSecond = second.copy(date = "2023-01-02T00:00:00+0000")
        createVersionOneDatabase(listOf(first, oldSecond))
        db.close()
        db = DatabaseModule().provideDatabase(context, Converters())
        assertEquals(setOf(first, oldSecond), db.clipDao().getAll().first().toSet())
        db.clipDao().insert(second)
        assertEquals(setOf(first, second), db.clipDao().getAll().first().toSet())
        db.clipDao().delete(first)
        assertEquals(listOf(second), db.clipDao().getAll().first())
        assertEquals(2, db.openHelper.readableDatabase.version)
    }

    @Test
    fun migrationKeepsLatestCopyOfDuplicateUrl() = runBlocking {
        val updated = first.copy(headline = HeadLineModel("Latest"), date = "2023-01-02T00:00:00+0000")
        createVersionOneDatabase(listOf(first, updated))
        db.close()
        db = DatabaseModule().provideDatabase(context, Converters())
        assertEquals(listOf(updated), db.clipDao().getAll().first())
    }

    private fun createVersionOneDatabase(articles: List<ArticleModel>) {
        context.deleteDatabase("clip_database")
        context.openOrCreateDatabase("clip_database", Context.MODE_PRIVATE, null).use { oldDb ->
            // Exact v1 table and Room identity from the original generated AppDataBase_Impl.
            oldDb.execSQL("CREATE TABLE ArticleModel (headline TEXT NOT NULL, url TEXT NOT NULL, " +
                "date TEXT NOT NULL, multimedia TEXT NOT NULL, PRIMARY KEY(date))")
            oldDb.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)")
            oldDb.execSQL("INSERT INTO room_master_table VALUES(42, '014cf1fb8d8c519f19dab6372a23d10a')")
            val converter = Converters()
            articles.forEach {
                oldDb.execSQL("INSERT INTO ArticleModel (headline, url, date, multimedia) VALUES (?, ?, ?, ?)",
                    arrayOf(converter.fromModel(it.headline), it.url, it.date, converter.fromModel(it.multimedia)))
            }
            oldDb.version = 1
        }
    }
}
