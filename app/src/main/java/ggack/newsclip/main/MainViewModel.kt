package ggack.newsclip.main

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import ggack.newsclip.Constants
import ggack.newsclip.data.EventModel
import ggack.newsclip.data.db.ClipRepository
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.data.network.NewsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val newsRepository: NewsRepository, private val clipRepository: ClipRepository) : ViewModel(){
    val liveEvent = MutableLiveData<EventModel?>(null)
    val liveListClip : LiveData<List<ArticleModel>> = clipRepository.clipList.asLiveData()

    private val _liveSearchText = MutableLiveData<String?>(null)
    val liveSearchText : LiveData<String?> = _liveSearchText
    val liveListArticle = liveSearchText.switchMap {
        newsRepository.getSearchResult(it?:"").cachedIn(viewModelScope).asLiveData()
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
    val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            _liveSearchText.postValue(query)
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }

    }
}