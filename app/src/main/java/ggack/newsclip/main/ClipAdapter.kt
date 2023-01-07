package ggack.newsclip.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ggack.newsclip.data.models.ArticleModel
import ggack.newsclip.databinding.ItemArticleBinding
import ggack.newsclip.ItemTouchHelper.ItemTouchHelperListener

class ClipAdapter(private val values: List<ArticleModel>, private val listener: ItemSelectListener)
    : RecyclerView.Adapter<ClipAdapter.ViewHolder>(), ItemTouchHelperListener {
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
        val item = values[position]
        holder.bind(item, position)
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : ArticleModel, position : Int) {
            binding.model = item
        }
    }

    override fun onSwipe(position: Int) {
        notifyItemChanged(position)
        listener.onSwipe(position, values[position])
    }
}