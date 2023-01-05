package ggack.newsclip

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ggack.newsclip.data.NewsApi
import kotlinx.coroutines.launch

class MainViewModel : ViewModel(){
    fun getArticles() {
        viewModelScope.launch {
            try {
                val result = NewsApi.retrofitService.articleSearch("kijN6KuzTKPChAwMTGXnx5GGrgfAX96X", "print_page:1","election")
                Log.e("log", result.toString())
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }
}