package ggack.newsclip.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import ggack.newsclip.Constants
import ggack.newsclip.R
import ggack.newsclip.webview.WebViewActivity
import ggack.newsclip.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        viewModel.liveEvent.observe(this) {
            val nav = supportFragmentManager.findFragmentById(R.id.container) as NavHostFragment?
            when(it?.action) {
                Constants.ACTION_TOAST -> {
                    Toast.makeText(this, it.param as String, Toast.LENGTH_SHORT).show()
                }
                Constants.ACTION_START_FRAGMENT_CLIP -> {
                    if(nav?.navController?.currentDestination?.id == R.id.articleListFragment)
                        nav.navController.navigate(
                            R.id.action_articleListFragment_to_clipListFragment,
                            null
                        )
                }
                Constants.ACTION_START_FRAGMENT_ARTICLE -> {
                    if(nav?.navController?.currentDestination?.id == R.id.clipListFragment)
                        nav.navController.navigate(
                            R.id.action_clipListFragment_to_articleListFragment,
                            null
                        )
                }
                Constants.ACTION_START_WEBVIEW -> {
                    val intent = Intent(this, WebViewActivity::class.java)
                    intent.putExtra("url", it.param as String)
                    startActivity(intent)
                }
            }
        }
    }
}