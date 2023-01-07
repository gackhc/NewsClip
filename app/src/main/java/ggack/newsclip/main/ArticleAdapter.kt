package ggack.newsclip.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ggack.newsclip.data.models.ArticleModel
import ggack.newsclip.databinding.ItemArticleBinding
import ggack.newsclip.ItemTouchHelper.ItemTouchHelperListener

class ArticleAdapter(private val listener: ItemSelectListener)
    : PagingDataAdapter<ArticleModel, ArticleAdapter.ViewHolder>(COMPARATOR),
    ItemTouchHelperListener {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(item, position)
        }
    }

    inner class ViewHolder(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : ArticleModel, position : Int) {
            binding.model = item
        }
    }

    override fun onSwipe(position: Int) {
        if(itemCount > position) {
            notifyItemChanged(position)
            listener.onSwipe(position, getItem(position))
        }
    }
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<ArticleModel>() {
            override fun areItemsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean =
                oldItem.date == newItem.date

            override fun areContentsTheSame(oldItem: ArticleModel, newItem: ArticleModel): Boolean =
                oldItem == newItem
        }
    }
}
