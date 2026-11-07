package ggack.newsclip.data.network

import ggack.newsclip.data.NewsModel
import ggack.newsclip.main.displayDate
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class ArticleJsonAdapterTest {
    private fun parse(json: String): NewsModel = NetworkModule().provideRetrofit()
        .responseBodyConverter<NewsModel>(NewsModel::class.java, emptyArray())
        .convert(json.toResponseBody())!!

    @Test fun currentServerResponseParsesWithAbsoluteImageUrls() {
        val json = javaClass.getResource("/nyt-current-article.json")!!.readText()
        val article = parse(json).response.docs.single()
        assertTrue(article.headline.text.isNotBlank())
        assertTrue(article.url.startsWith("https://www.nytimes.com/"))
        assertEquals(2, article.multimedia.size)
        assertTrue(article.multimedia.first().url.startsWith("https://static01.nyt.com/"))
    }

    @Test fun legacyArrayAndMissingImagesRemainSupported() {
        for (media in listOf("[]", "null", "{}", """[{"url":"images/photo.jpg"}]""")) {
            val article = parse("""{"status":"OK","response":{"docs":[{
                "headline":{"main":"Test"},"web_url":"https://example.test/article",
                "pub_date":"2026-11-06T10:03:49Z","multimedia":$media}]}}""").response.docs.single()
            assertEquals(if (media.contains("photo.jpg")) 1 else 0, article.multimedia.size)
        }
    }

    @Test fun currentUtcDateAndLegacyOffsetDisplayTheSameDate() {
        val current = displayDate("2026-11-06T10:03:49Z")
        assertEquals(displayDate("2026-11-06T10:03:49+0000"), current)
        assertTrue(current.matches(Regex("\\d{4}년 \\d{2}월 \\d{2}일")))
    }
}
