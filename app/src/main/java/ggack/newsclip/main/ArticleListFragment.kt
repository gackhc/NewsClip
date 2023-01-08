package ggack.newsclip.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.databinding.FragmentArticleListBinding
import ggack.newsclip.itemtouchhelper.ItemTouchHelperCallback
import ggack.newsclip.webview.WebViewActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class ArticleListFragment : Fragment() {
    private var binding : FragmentArticleListBinding? = null
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleListBinding.inflate(inflater, container, false)
        binding?.vm = viewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        binding?.list?.layoutManager = LinearLayoutManager(context)
        val adapter = ArticleAdapter(object : ItemSelectListener {
            override fun onSelected(data : Any?) {
                val url = (data as String?)?:""
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }
            override fun onSwipe(position: Int, data : Any?) {
                data?.let {
                    viewModel.insertArticleToClip(data as ArticleModel)
                    Toast.makeText(requireContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
        ItemTouchHelper(ItemTouchHelperCallback(adapter)).attachToRecyclerView(binding?.list)
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {state ->
                when (state.refresh) {
                    is LoadState.Loading -> {
                        binding?.swipeRefreshLayout?.isRefreshing = true
                    }
                    is LoadState.NotLoading -> {
                        binding?.swipeRefreshLayout?.isRefreshing = false
                    }
                    is LoadState.Error -> {
                        binding?.swipeRefreshLayout?.isRefreshing = false
                        Toast.makeText(requireContext(), (state.refresh as LoadState.Error).error.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        viewModel.liveListArticle.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                adapter.submitData(it)
            }
        }
        binding?.list?.adapter = adapter

        binding?.swipeRefreshLayout?.setOnRefreshListener {
            adapter.refresh()
        }

        return binding?.root
    }
}