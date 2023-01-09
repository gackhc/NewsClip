package ggack.newsclip.main

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import ggack.newsclip.Constants
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.data.Converters
import ggack.newsclip.data.HeadLineModel
import ggack.newsclip.data.MultimediaModel
import ggack.newsclip.data.db.AppDataBase
import ggack.newsclip.data.db.ClipDao
import ggack.newsclip.data.db.ClipRepository
import ggack.newsclip.data.db.DatabaseModule
import ggack.newsclip.data.network.NetworkModule
import ggack.newsclip.data.network.NewsRepository
import ggack.newsclip.getOrAwaitValue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class MainViewModelTest {
    private lateinit var viewModel : MainViewModel
    private lateinit var dao: ClipDao
    private lateinit var db: AppDataBase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        val networkModule = NetworkModule()
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDataBase::class.java)
            // Allowing main thread queries, just for testing.
            .addTypeConverter(Converters())
            .allowMainThreadQueries()
            .build()
        dao = db.clipDao()

        viewModel = MainViewModel(
            NewsRepository(networkModule.provideNewsApiInterface(networkModule.provideRetrofit())),
            ClipRepository(dao)
        )
    }

    @Test
    fun startClipFragment() {
        viewModel.startClipFragment()
        val result = viewModel.liveEvent.getOrAwaitValue()
        assertThat(result?.action, `is`(Constants.ACTION_START_FRAGMENT_CLIP))
    }

    @Test
    fun startArticleFragment() {
        viewModel.startArticleFragment()
        val result = viewModel.liveEvent.getOrAwaitValue()
        assertThat(result?.action, `is`(Constants.ACTION_START_FRAGMENT_ARTICLE))
    }

    private val testArticle = ArticleModel(HeadLineModel("headline_text"), "url_text", "pub_date_text",
        listOf(MultimediaModel("img_url_test")))

    private fun insertArticleToClip() {
        viewModel.insertArticleToClip(testArticle)
        val value = viewModel.liveListClip.getOrAwaitValue()
        assert(value.contains(testArticle))
    }

    @Test
    fun deleteArticleFromClip() {
        insertArticleToClip()
        viewModel.deleteArticleFromClip(testArticle)
        val value = viewModel.liveListClip.getOrAwaitValue()
        assert(!value.contains(testArticle))
    }
}