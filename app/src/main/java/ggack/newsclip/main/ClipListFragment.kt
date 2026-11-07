package ggack.newsclip.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import ggack.newsclip.data.ArticleModel
import ggack.newsclip.databinding.FragmentArticleListBinding
import ggack.newsclip.itemtouchhelper.ItemTouchHelperCallback
import ggack.newsclip.webview.WebViewActivity

/**
 * A fragment representing a list of Items.
 */
class ClipListFragment : Fragment() {
    private var binding : FragmentArticleListBinding? = null
    private val viewModel by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleListBinding.inflate(inflater, container, false)
        binding?.vm = viewModel
        binding?.lifecycleOwner = viewLifecycleOwner
        binding?.swipeRefreshLayout?.isEnabled = false
        binding?.list?.layoutManager = LinearLayoutManager(context)
        val adapter = ClipAdapter(object : ItemSelectListener {
            override fun onSelected(data : Any?) {
                val url = (data as String?)?:""
                val intent = Intent(requireContext(), WebViewActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }

            override fun onSwipe(position: Int, data : Any?) {
                data?.let {
                    viewModel.deleteArticleFromClip(data as ArticleModel)
                    Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
        })
        ItemTouchHelper(ItemTouchHelperCallback(adapter)).attachToRecyclerView(binding?.list)
        binding?.list?.adapter = adapter

        viewModel.liveListClip.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        return binding?.root
    }

    override fun onDestroyView() {
        binding?.list?.adapter = null
        binding = null
        super.onDestroyView()
    }
}
