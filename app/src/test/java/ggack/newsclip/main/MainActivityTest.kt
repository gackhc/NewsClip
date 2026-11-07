package ggack.newsclip.main

import android.content.Context
import android.os.Looper
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.testing.*
import ggack.newsclip.R
import ggack.newsclip.data.*
import ggack.newsclip.data.db.*
import ggack.newsclip.data.network.NetworkModule
import ggack.newsclip.data.network.NewsService
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import java.time.Duration

@HiltAndroidTest
@UninstallModules(NetworkModule::class, DatabaseModule::class)
@Config(application = HiltTestApplication::class)
@LooperMode(LooperMode.Mode.PAUSED)
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val service: NewsService = object : NewsService {
        override suspend fun articleSearch(key: String, page: Int, query: String?) =
            NewsModel("OK", ResponseModel(emptyList()))
    }

    @BindValue
    lateinit var dao: ClipDao
    private lateinit var db: AppDataBase
    private lateinit var scenario: ActivityScenario<MainActivity>
    private val first = ArticleModel(HeadLineModel("First"), "https://example.test/first",
        "2023-01-01T00:00:00+0000", emptyList())
    private val second = first.copy(headline = HeadLineModel("Second"), url = "https://example.test/second")

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),
            AppDataBase::class.java).addTypeConverter(Converters()).allowMainThreadQueries().build()
        dao = db.clipDao()
        hiltRule.inject()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun teardown() {
        if (::scenario.isInitialized) scenario.close()
        db.close()
    }

    @Test
    fun repeatedTabSwitchesDoNotAccumulateScreensAndBackUpdatesSelection() {
        repeat(6) {
            clickTab(R.id.btnFavorite, R.id.clipListFragment)
            clickTab(R.id.btnHome, R.id.articleListFragment)
            scenario.onActivity { assertNull(navHost(it).navController.previousBackStackEntry) }
        }
        clickTab(R.id.btnFavorite, R.id.clipListFragment)
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitDestination(R.id.articleListFragment)
        scenario.onActivity {
            assertTrue(it.findViewById<MaterialButton>(R.id.btnHome).isChecked)
            assertFalse(it.findViewById<MaterialButton>(R.id.btnFavorite).isChecked)
        }
    }

    @Test
    fun recreationAfterBackDoesNotReplayNavigationEvent() {
        clickTab(R.id.btnFavorite, R.id.clipListFragment)
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        awaitDestination(R.id.articleListFragment)
        scenario.recreate()
        awaitDestination(R.id.articleListFragment)
        scenario.onActivity {
            assertTrue(it.findViewById<MaterialButton>(R.id.btnHome).isChecked)
            assertNull(ViewModelProvider(it)[MainViewModel::class.java].liveEvent.value)
        }
    }

    @Test
    fun clipListReflectsChangesWhileVisibleAndAfterReturningAndRecreating() {
        runBlocking { dao.insert(first); dao.insert(second) }
        clickTab(R.id.btnFavorite, R.id.clipListFragment)
        awaitClips(setOf(first, second))
        val updated = first.copy(headline = HeadLineModel("Updated headline"))
        runBlocking { dao.insert(updated) }
        awaitClips(setOf(updated, second))
        runBlocking { dao.delete(second) }
        awaitClips(setOf(updated))

        clickTab(R.id.btnHome, R.id.articleListFragment)
        runBlocking { dao.delete(updated) }
        clickTab(R.id.btnFavorite, R.id.clipListFragment)
        awaitClips(emptySet())
        scenario.recreate()
        awaitDestination(R.id.clipListFragment)
        awaitClips(emptySet())
        scenario.onActivity {
            assertTrue(it.findViewById<MaterialButton>(R.id.btnFavorite).isChecked)
        }
    }

    @Test
    fun leavingEachScreenDetachesAdapterAndCancelsOldViewScope() {
        lateinit var oldList: RecyclerView
        lateinit var oldJob: Job
        scenario.onActivity {
            val fragment = navHost(it).childFragmentManager.primaryNavigationFragment!!
            oldList = fragment.requireView().findViewById(R.id.list)
            oldJob = fragment.viewLifecycleOwner.lifecycleScope.coroutineContext[Job]!!
            (oldList.adapter as ArticleAdapter).onSwipe(RecyclerView.NO_POSITION)
            (oldList.adapter as ArticleAdapter).onSwipe(oldList.adapter!!.itemCount)
        }
        clickTab(R.id.btnFavorite, R.id.clipListFragment)
        await { oldJob.isCancelled && oldList.adapter == null }
        scenario.onActivity {
            val fragment = navHost(it).childFragmentManager.primaryNavigationFragment!!
            oldList = fragment.requireView().findViewById(R.id.list)
            oldJob = fragment.viewLifecycleOwner.lifecycleScope.coroutineContext[Job]!!
            (oldList.adapter as ClipAdapter).onSwipe(RecyclerView.NO_POSITION)
            (oldList.adapter as ClipAdapter).onSwipe(oldList.adapter!!.itemCount)
        }
        clickTab(R.id.btnHome, R.id.articleListFragment)
        await { oldJob.isCancelled && oldList.adapter == null }
    }

    private fun clickTab(buttonId: Int, destinationId: Int) {
        scenario.onActivity { it.findViewById<MaterialButton>(buttonId).performClick() }
        awaitDestination(destinationId)
    }

    private fun awaitDestination(id: Int) = await { activity ->
        navHost(activity).navController.currentDestination?.id == id
    }

    private fun awaitClips(expected: Set<ArticleModel>) = await { activity ->
        val view = navHost(activity).childFragmentManager.primaryNavigationFragment?.view
        val adapter = view?.findViewById<RecyclerView>(R.id.list)?.adapter as? ClipAdapter
        adapter != null && adapter.currentList.toSet() == expected
    }

    private fun await(condition: (MainActivity) -> Boolean) {
        val deadline = System.nanoTime() + 5_000_000_000L
        do {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))
            var satisfied = false
            scenario.onActivity { satisfied = condition(it) }
            if (satisfied) return
            Thread.sleep(10)
        } while (System.nanoTime() < deadline)
        fail("Screen did not reach the expected state within five seconds")
    }

    private fun navHost(activity: MainActivity) =
        activity.supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment
}
