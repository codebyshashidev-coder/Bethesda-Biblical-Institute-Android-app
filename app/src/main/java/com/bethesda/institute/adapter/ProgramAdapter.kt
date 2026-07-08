package com.bethesda.institute.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bethesda.institute.R
import com.bethesda.institute.model.Program

class ProgramAdapter(
    private val items: List<Program>,
    private val onClick: (Program) -> Unit
) : RecyclerView.Adapter<ProgramAdapter.ProgramViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_program_card, parent, false)
        return ProgramViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgramViewHolder, position: Int) {
        holder.bind(items[position], onClick)
    }

    override fun getItemCount(): Int = items.size

    class ProgramViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val level: TextView = itemView.findViewById(R.id.programLevel)
        private val title: TextView = itemView.findViewById(R.id.programTitle)
        private val description: TextView = itemView.findViewById(R.id.programDescription)

        fun bind(program: Program, onClick: (Program) -> Unit) {
            level.text = program.level
            title.text = program.title
            description.text = program.description
            itemView.setOnClickListener { onClick(program) }
        }
    }
}
