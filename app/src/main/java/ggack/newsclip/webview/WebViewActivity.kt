package ggack.newsclip.webview

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ggack.newsclip.main.NewsclipTheme

class WebViewActivity : ComponentActivity() {
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsclipTheme {
                BackHandler {
                    val web = webView
                    if (web?.canGoBack() == true) web.goBack() else finish()
                }
                AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                    WebView(context).apply {
                        webView = this
                        webViewClient = WebViewClient()
                        if (savedInstanceState?.getBundle("web_state")?.let { restoreState(it) } == null) {
                            intent.getStringExtra("url")?.let { loadUrl(it) }
                        }
                    }
                })
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView?.let { web -> outState.putBundle("web_state", Bundle().apply { web.saveState(this) }) }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView?.stopLoading()
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}
