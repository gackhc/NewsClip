package ggack.newsclip.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ggack.newsclip.webview.WebViewActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsclipTheme {
                NewsclipApp(viewModel) { url ->
                    startActivity(Intent(this, WebViewActivity::class.java).putExtra("url", url))
                }
            }
        }
    }
}
