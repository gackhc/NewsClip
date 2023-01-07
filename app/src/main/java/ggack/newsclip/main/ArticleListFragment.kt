package ggack.newsclip.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import ggack.newsclip.data.models.ArticleModel
import ggack.newsclip.databinding.FragmentArticleListBinding
import ggack.newsclip.ItemTouchHelper.ItemTouchHelperCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class ArticleListFragment : Fragment() {
    private var binding : FragmentArticleListBinding? = null
    private val viewModel by activityViewModels<MainViewModel>()
    private val mListItems = mutableListOf<ArticleModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleListBinding.inflate(inflater, container, false)

        binding?.list?.layoutManager = LinearLayoutManager(context)
        val adapter = ArticleAdapter(object : ItemSelectListener {
            override fun onSelected(position: Int) {

            }
            override fun onSwipe(position: Int, data : Any?) {
                data?.let {
                    viewModel.insertArticleToClip(data as ArticleModel)
                    Toast.makeText(requireContext(), "저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
        ItemTouchHelper(ItemTouchHelperCallback(adapter)).attachToRecyclerView(binding?.list)
        CoroutineScope(Dispatchers.Main).launch {
            viewModel.flowListArticle.collectLatest {
                mListItems.toList().forEach {
                    Log.e("debug", it.headline.text)
                }
                binding?.swipeRefreshLayout?.isRefreshing = false
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