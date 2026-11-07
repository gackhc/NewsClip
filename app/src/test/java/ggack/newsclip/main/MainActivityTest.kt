package ggack.newsclip.main

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.*
import ggack.newsclip.data.*
import ggack.newsclip.data.db.*
import ggack.newsclip.data.network.NetworkModule
import ggack.newsclip.data.network.NewsService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@HiltAndroidTest
@UninstallModules(NetworkModule::class, DatabaseModule::class)
@Config(application = HiltTestApplication::class, sdk = [32])
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createEmptyComposeRule()
    private val first = ArticleModel(HeadLineModel("First article"), "https://example.test/first",
        "2023-01-01T00:00:00+0000", emptyList())
    @Volatile private var receivedQuery: String? = null
    @Volatile private var failure: Exception? = null
    @BindValue @JvmField val service: NewsService = object : NewsService {
        override suspend fun articleSearch(key: String, page: Int, query: String?): NewsModel {
            receivedQuery = query
            failure?.let { throw it }
            return NewsModel("OK", ResponseModel(listOf(first)))
        }
    }
    @BindValue lateinit var dao: ClipDao
    private lateinit var db: AppDataBase
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),
            AppDataBase::class.java).addTypeConverter(Converters()).allowMainThreadQueries().build()
        dao = db.clipDao()
        hiltRule.inject()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After fun teardown() {
        if (::scenario.isInitialized) scenario.close()
        db.close()
    }

    @Test fun repeatedTabsAndBackKeepSelectionConsistent() {
        repeat(5) {
            compose.onNodeWithTag("clip_tab").performClick().assertIsSelected()
            compose.onNodeWithTag("home_tab").performClick().assertIsSelected()
        }
        compose.onNodeWithTag("clip_tab").performClick().assertIsSelected()
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("home_tab").assertIsSelected()
        scenario.recreate()
        compose.onNodeWithTag("home_tab").assertIsSelected()
    }

    @Test fun selectedTabAndSearchTextSurviveRecreation() {
        compose.onNodeWithTag("search").performTextInput("science")
        compose.onNodeWithTag("clip_tab").performClick()
        scenario.recreate()
        compose.onNodeWithTag("clip_tab").assertIsSelected()
        compose.onNodeWithTag("search").assertTextContains("science")
    }

    @Test fun submittedSearchCallsRepositoryAndReturnsHome() {
        compose.onNodeWithTag("clip_tab").performClick()
        compose.onNodeWithTag("search").performTextInput("space")
        compose.onNodeWithTag("search").performImeAction()
        compose.waitUntil(5000) { receivedQuery == "space" }
        compose.onNodeWithTag("home_tab").assertIsSelected()
        awaitHeadline("First article")
    }

    @Test fun swipeSavesAndDeletesArticleAndDbUpdatesRender() {
        awaitHeadline("First article")
        compose.onNodeWithTag("article:${first.url}").performTouchInput { swipeLeft() }
        compose.waitUntil(5000) { runBlocking { dao.getAll().first().any { it.url == first.url } } }
        compose.onNodeWithTag("clip_tab").performClick()
        awaitHeadline("First article")
        runBlocking { dao.insert(first.copy(headline = HeadLineModel("Updated article"))) }
        awaitHeadline("Updated article")
        compose.onNodeWithTag("article:${first.url}").performTouchInput { swipeRight() }
        compose.waitUntil(5000) { runBlocking { dao.getAll().first().none { it.url == first.url } } }
        compose.onNodeWithText("저장한 기사가 없습니다.").assertExists()
    }

    private fun awaitHeadline(text: String) {
        compose.waitUntil(5000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun authenticationFailureShowsSpecificMessageAndCanRetry() {
        awaitHeadline("First article")
        failure = retrofit2.HttpException(retrofit2.Response.error<NewsModel>(401,
            "Invalid ApiKey".toResponseBody()))
        compose.onNodeWithTag("search").performTextInput("new query")
        compose.onNodeWithTag("search").performImeAction()
        awaitHeadline("뉴스 서비스 인증에 실패했습니다. 설정된 키를 확인해 주세요.")
        failure = null
        compose.onNodeWithText("다시 시도").performClick()
        awaitHeadline("First article")
    }
}
