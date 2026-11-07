package ggack.newsclip.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import ggack.newsclip.R
import ggack.newsclip.data.ArticleModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.io.IOException
import retrofit2.HttpException

@Composable
fun NewsclipTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors(primary = Color(0xFFBB86FC))
        else lightColors(primary = Color(0xFF6200EE)), content = content)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NewsclipApp(viewModel: MainViewModel, openArticle: (String) -> Unit) {
    var clipsSelected by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val focus = LocalFocusManager.current
    val articles = remember(viewModel) { viewModel.liveListArticle.asFlow() }.collectAsLazyPagingItems()
    val clips by viewModel.liveListClip.observeAsState(emptyList())
    val articleScroll = rememberLazyListState()
    val clipScroll = rememberLazyListState()
    val refresh = articles.loadState.refresh
    val pullRefresh = rememberPullRefreshState(refresh is LoadState.Loading, { articles.refresh() })
    BackHandler(clipsSelected) { clipsSelected = false }
    Scaffold(bottomBar = {
        BottomNavigation {
            BottomNavigationItem(selected = !clipsSelected, onClick = { clipsSelected = false },
                modifier = Modifier.testTag("home_tab"), label = { Text("Home") },
                icon = { Icon(painterResource(R.drawable.ic_home), contentDescription = null) })
            BottomNavigationItem(selected = clipsSelected, onClick = { clipsSelected = true },
                modifier = Modifier.testTag("clip_tab"), label = { Text("Clip") },
                icon = { Icon(painterResource(R.drawable.ic_favorite), contentDescription = null) })
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                label = { Text("기사 검색") }, modifier = Modifier.fillMaxWidth().padding(12.dp).testTag("search"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.search(query)
                    clipsSelected = false
                    focus.clearFocus()
                }))
            if (clipsSelected) {
                if (clips.isEmpty()) EmptyMessage("저장한 기사가 없습니다.")
                LazyColumn(state = clipScroll, modifier = Modifier.fillMaxSize().testTag("clip_list")) {
                    items(clips, key = { it.url }) { article ->
                        SwipeArticle(article, "삭제", { viewModel.deleteArticleFromClip(article) }, openArticle)
                    }
                }
            } else {
                Box(Modifier.fillMaxSize().pullRefresh(pullRefresh).testTag("article_list")) {
                    LazyColumn(state = articleScroll, modifier = Modifier.fillMaxSize()) {
                        items(articles.itemCount) { index ->
                            articles[index]?.let { article ->
                                key(article.url) {
                                    SwipeArticle(article, "저장", { viewModel.insertArticleToClip(article) }, openArticle)
                                }
                            }
                        }
                        if (articles.loadState.append is LoadState.Loading) {
                            item { CircularProgressIndicator(Modifier.padding(16.dp)) }
                        }
                        if (articles.loadState.append is LoadState.Error) {
                            item { RetryMessage((articles.loadState.append as LoadState.Error).error) { articles.retry() } }
                        }
                    }
                    if (refresh is LoadState.Error) {
                        RetryMessage(refresh.error) { articles.retry() }
                    } else if (refresh is LoadState.NotLoading && articles.itemCount == 0) {
                        EmptyMessage("검색 결과가 없습니다.")
                    }
                    PullRefreshIndicator(refresh is LoadState.Loading, pullRefresh, Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(24.dp))
}

@Composable
private fun RetryMessage(error: Throwable, retry: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Text(when {
            error is HttpException && error.code() in listOf(401, 403) -> "뉴스 서비스 인증에 실패했습니다. 설정된 키를 확인해 주세요."
            error is HttpException && error.code() == 429 -> "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
            error is IOException -> "네트워크 연결을 확인해 주세요."
            error is IllegalStateException -> "뉴스 서비스 키가 설정되지 않았습니다."
            else -> "기사를 불러오지 못했습니다."
        })
        TextButton(onClick = retry) { Text("다시 시도") }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeArticle(article: ArticleModel, action: String, onSwipe: () -> Unit, openArticle: (String) -> Unit) {
    val dismiss = rememberDismissState()
    LaunchedEffect(dismiss.currentValue) {
        if (dismiss.currentValue != DismissValue.Default) {
            onSwipe()
            dismiss.reset()
        }
    }
    SwipeToDismiss(state = dismiss, modifier = Modifier.testTag("article:${article.url}"),
        background = {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colors.primary).padding(20.dp),
                contentAlignment = if (dismiss.dismissDirection == DismissDirection.StartToEnd)
                    Alignment.CenterStart else Alignment.CenterEnd) {
                Text(action, color = MaterialTheme.colors.onPrimary)
            }
        }, dismissContent = {
            Surface {
                Row(Modifier.fillMaxWidth().clickable { openArticle(article.url) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    val image = article.multimedia.firstOrNull()?.url?.let {
                        if (it.startsWith("https://") || it.startsWith("http://")) it else "https://static01.nyt.com/$it"
                    }
                    AsyncImage(model = image, contentDescription = null,
                        error = painterResource(R.drawable.ic_broken_image),
                        placeholder = painterResource(R.drawable.ic_broken_image),
                        modifier = Modifier.size(96.dp, 64.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(article.headline.text, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.body1)
                        Text(remember(article.date) { displayDate(article.date) },
                            style = MaterialTheme.typography.caption, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        })
}

internal fun displayDate(value: String): String = runCatching {
    val normalized = if (value.endsWith("Z")) value.dropLast(1) + "+0000" else value
    val parsed = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(normalized)
    parsed?.let { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(it) } ?: value
}.getOrDefault(value)
