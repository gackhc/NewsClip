package ggack.newsclip.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ggack.newsclip.Constants
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.data.NewsApi
import ggack.newsclip.data.models.EventModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel(){
    val liveEvent = MutableLiveData<EventModel?>(null)
    private val _liveListArticle = MutableLiveData<List<ArticleModel>>(listOf())
    val liveListArticle : LiveData<List<ArticleModel>> =(_liveListArticle)
    fun getArticles() {
        viewModelScope.launch {
            try {
                val result = NewsApi.retrofitService.articleSearch("kijN6KuzTKPChAwMTGXnx5GGrgfAX96X", "print_page:1","election")
                result.response?.docs?.let{
                    _liveListArticle.postValue(it)
                } ?.run {
                    Log.e("MainViewModel", "${result.status} failed to get articles")
                }
            } catch (e : Exception) {
                Log.e("MainViewModel", "failed to get articles : $e ")
            }
        }
    }

    fun startClipFragment() {
        liveEvent.postValue(EventModel(Constants.ACTION_START_FRAGMENT_CLIP))
    }
    fun startArticleFragment() {
        liveEvent.postValue(EventModel(Constants.ACTION_START_FRAGMENT_ARTICLE))
    }
}