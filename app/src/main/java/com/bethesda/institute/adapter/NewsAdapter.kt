package com.bethesda.institute.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bethesda.institute.R
import com.bethesda.institute.model.NewsItem
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class NewsAdapter(
    private val items: MutableList<NewsItem> = mutableListOf(),
    private val onClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    fun submitList(newItems: List<NewsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_card, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.newsImage)
        private val category: TextView = itemView.findViewById(R.id.newsCategory)
        private val title: TextView = itemView.findViewById(R.id.newsTitle)
        private val date: TextView = itemView.findViewById(R.id.newsDate)

        fun bind(item: NewsItem, onClick: (NewsItem) -> Unit) {
            title.text = item.title
            category.text = item.category.uppercase(Locale.getDefault()).replace("_", " ")
            date.text = formatDate(item.createdAt)

            if (item.image != null) {
                Glide.with(image.context).load(item.image).centerCrop().into(image)
            } else {
                image.setImageDrawable(null)
                image.setBackgroundColor(itemView.context.getColor(R.color.surface_container))
            }

            itemView.setOnClickListener { onClick(item) }
        }

        private fun formatDate(raw: String): String {
            return try {
                val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val output = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                output.format(input.parse(raw) ?: return raw)
            } catch (e: Exception) {
                raw
            }
        }
    }
}
