package com.bethesda.institute.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bethesda.institute.R
import com.bethesda.institute.model.Testimonial
import com.bumptech.glide.Glide

class TestimonialAdapter(
    private val items: MutableList<Testimonial> = mutableListOf()
) : RecyclerView.Adapter<TestimonialAdapter.TestimonialViewHolder>() {

    fun submitList(newItems: List<Testimonial>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestimonialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_testimonial_card, parent, false)
        return TestimonialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestimonialViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class TestimonialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stars: TextView = itemView.findViewById(R.id.testimonialStars)
        private val quote: TextView = itemView.findViewById(R.id.testimonialQuote)
        private val avatar: ImageView = itemView.findViewById(R.id.testimonialAvatar)
        private val name: TextView = itemView.findViewById(R.id.testimonialName)
        private val role: TextView = itemView.findViewById(R.id.testimonialRole)

        fun bind(t: Testimonial) {
            stars.text = "★".repeat(t.rating.coerceIn(0, 5)) + "☆".repeat(5 - t.rating.coerceIn(0, 5))
            quote.text = "\u201C${t.quote}\u201D"
            name.text = t.name
            role.text = t.role

            if (t.photo != null) {
                Glide.with(avatar.context).load(t.photo).circleCrop().into(avatar)
            } else {
                avatar.setImageDrawable(null)
                avatar.setBackgroundColor(itemView.context.getColor(R.color.surface_container))
            }
        }
    }
}
