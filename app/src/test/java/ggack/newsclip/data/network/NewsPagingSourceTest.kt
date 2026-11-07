package ggack.newsclip.data.network

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import ggack.newsclip.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

class NewsPagingSourceTest {
    private class FakeService : NewsService {
        var docs = List(10) { article(it) }
        var failure: Exception? = null
        val pages = mutableListOf<Int>()
        var query: String? = null

        override suspend fun articleSearch(key: String, page: Int, query: String?): NewsModel {
            pages.add(page)
            this.query = query
            failure?.let { throw it }
            return NewsModel("OK", ResponseModel(docs))
        }
    }

    @Test
    fun firstAndNextPageUseZeroBasedPageNumbers() = runBlocking {
        val service = FakeService()
        val source = NewsPagingSource(service, "climate change", "test-key")
        val first = source.load(PagingSource.LoadParams.Refresh(null, 30, false))
            as PagingSource.LoadResult.Page
        val second = source.load(PagingSource.LoadParams.Append(first.nextKey!!, 10, false))
            as PagingSource.LoadResult.Page
        assertEquals(listOf(0, 1), service.pages)
        assertEquals("climate change", service.query)
        assertNull(first.prevKey)
        assertEquals(0, second.prevKey)
        assertEquals(2, second.nextKey)
    }

    @Test
    fun partialAndEmptyPagesEndPagination() = runBlocking {
        for (count in listOf(3, 0)) {
            val service = FakeService().apply { docs = List(count) { article(it) } }
            val result = NewsPagingSource(service, "news", "test-key")
                .load(PagingSource.LoadParams.Append(4, 10, false)) as PagingSource.LoadResult.Page
            assertEquals(count, result.data.size)
            assertNull(result.nextKey)
        }
    }

    @Test
    fun stopsAtApiPageLimitEvenWhenLastPageIsFull() = runBlocking {
        val result = NewsPagingSource(FakeService(), "news", "test-key")
            .load(PagingSource.LoadParams.Append(99, 10, false)) as PagingSource.LoadResult.Page
        assertNull(result.nextKey)
    }

    @Test
    fun networkFailureBecomesLoadError() = runBlocking {
        val error = IOException("offline")
        val service = FakeService().apply { failure = error }
        val result = NewsPagingSource(service, "news", "test-key")
            .load(PagingSource.LoadParams.Refresh(null, 10, false)) as PagingSource.LoadResult.Error
        assertSame(error, result.throwable)
    }

    @Test
    fun cancellationIsNotReportedAsNetworkFailure() {
        val service = FakeService().apply { failure = CancellationException("cancelled") }
        assertThrows(CancellationException::class.java) {
            runBlocking {
                NewsPagingSource(service, "news", "test-key")
                    .load(PagingSource.LoadParams.Refresh(null, 10, false))
            }
        }
    }

    @Test
    fun missingKeyFailsBeforeMakingRequest() = runBlocking {
        val service = FakeService()
        val result = NewsPagingSource(service, "news", "")
            .load(PagingSource.LoadParams.Refresh(null, 10, false))
        assertTrue(result is PagingSource.LoadResult.Error)
        assertTrue(service.pages.isEmpty())
    }

    @Test
    fun refreshKeepsCurrentPage() {
        val source = NewsPagingSource(FakeService(), "news", "test-key")
        val state = PagingState(
            listOf(PagingSource.LoadResult.Page(List(10) { article(it) }, 2, 4)),
            5, PagingConfig(10), 0
        )
        assertEquals(3, source.getRefreshKey(state))
    }

    @Test
    fun retrofitSendsPageAndQueryWithoutPrintPageFilter() = runBlocking {
        var request: Request? = null
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            request = chain.request()
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(200).message("OK")
                .body("""{"status":"OK","response":{"docs":[]}}"""
                    .toResponseBody("application/json".toMediaType()))
                .build()
        }.build()
        try {
            val service = Retrofit.Builder().baseUrl("https://example.test/").client(client)
                .addConverterFactory(MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()))
                .build().create(NewsService::class.java)
            NewsPagingSource(service, "science & space", "test-key")
                .load(PagingSource.LoadParams.Append(2, 10, false))
            assertEquals("2", request!!.url.queryParameter("page"))
            assertEquals("science & space", request!!.url.queryParameter("q"))
            assertEquals("test-key", request!!.url.queryParameter("api-key"))
            assertNull(request!!.url.queryParameter("fq"))
        } finally {
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        }
    }

    companion object {
        private fun article(index: Int) = ArticleModel(
            HeadLineModel("Article $index"), "https://example.test/$index",
            "2023-01-01T00:00:00+0000", emptyList()
        )
    }
}
