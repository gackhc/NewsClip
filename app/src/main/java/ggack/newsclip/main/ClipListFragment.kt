package ggack.newsclip.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import ggack.newsclip.data.models.ArticleModel
import ggack.newsclip.databinding.FragmentArticleListBinding
import ggack.newsclip.ItemTouchHelper.ItemTouchHelperCallback

/**
 * A fragment representing a list of Items.
 */
class ClipListFragment : Fragment() {
    private var binding : FragmentArticleListBinding? = null
    private val viewModel by activityViewModels<MainViewModel>()
    private var columnCount = 1
    private val mListItems = mutableListOf<ArticleModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentArticleListBinding.inflate(inflater, container, false)

        binding?.list?.layoutManager = LinearLayoutManager(context)
        val adapter = ClipAdapter(mListItems, object : ItemSelectListener {
            override fun onSelected(position: Int) {
            }

            override fun onSwipe(position: Int, data : Any?) {
                data?.let {
                    viewModel.deleteArticleFromClip(data as ArticleModel)
                    mListItems.removeAt(position)
                    binding?.list?.adapter?.notifyItemRemoved(position)
                    Toast.makeText(requireContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
        })
        ItemTouchHelper(ItemTouchHelperCallback(adapter)).attachToRecyclerView(binding?.list)
        binding?.list?.adapter = adapter

        viewModel.liveListClip.observe(viewLifecycleOwner) {
            it.forEach { article ->
                if(!mListItems.contains(article)) {
                    mListItems.add(article)
                    binding?.list?.adapter?.notifyItemInserted(mListItems.lastIndex)
                }
            }
        }
        return binding?.root
    }
}