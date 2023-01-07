package ggack.newsclip.main

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ggack.newsclip.Constants
import ggack.newsclip.data.db.ClipRepository
import ggack.newsclip.data.models.ArticleModel
import ggack.newsclip.data.EventModel
import ggack.newsclip.data.network.NewsRepository
import ggack.newsclip.data.network.NewsService
import kotlinx.coroutines.flow.cache
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val newsRepository: NewsRepository, private val clipRepository: ClipRepository) : ViewModel(){
    val liveEvent = MutableLiveData<EventModel?>(null)
    //private val _liveListArticle = MutableLiveData<List<ArticleModel>>(listOf())
    //val liveListArticle : LiveData<List<ArticleModel>> = (_liveListArticle)
    val liveListClip : LiveData<List<ArticleModel>> = clipRepository.clipList.asLiveData()
    val flowListArticle = newsRepository.getSearchResult().cachedIn(viewModelScope)
    fun getArticles() {
//        viewModelScope.launch {
//            try {
//                val result = newsService.articleSearch("kijN6KuzTKPChAwMTGXnx5GGrgfAX96X", "print_page:1","election")
//                result.response?.docs?.let{
//                    _liveListArticle.postValue(it)
//                } ?.run {
//                    Log.e("MainViewModel", "${result.status} failed to get articles")
//                }
//            } catch (e : Exception) {
//                Log.e("MainViewModel", "failed to get articles : $e ")
//            }
//        }
    }

    fun startClipFragment() {
        liveEvent.postValue(EventModel(Constants.ACTION_START_FRAGMENT_CLIP))
    }
    fun startArticleFragment() {
        liveEvent.postValue(EventModel(Constants.ACTION_START_FRAGMENT_ARTICLE))
    }
    fun insertArticleToClip(article : ArticleModel) {
        viewModelScope.launch {
            clipRepository.insert(article)
        }
    }
    fun deleteArticleFromClip(article: ArticleModel) {
        viewModelScope.launch {
            clipRepository.delete(article)
        }
    }
}