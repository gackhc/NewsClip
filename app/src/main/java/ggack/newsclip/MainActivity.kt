package ggack.newsclip

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ggack.newsclip.data.NewsApi
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = MainViewModel()
        viewModel.getArticles()
    }

    override fun onResume() {
        super.onResume()
    }
}