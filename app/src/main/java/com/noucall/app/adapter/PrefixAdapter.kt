package com.noucall.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noucall.app.R

class PrefixAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<String, PrefixAdapter.PrefixViewHolder>(PrefixDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrefixViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prefix, parent, false)
        return PrefixViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrefixViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PrefixViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPrefix: TextView = itemView.findViewById(R.id.tv_prefix)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(prefix: String) {
            tvPrefix.text = prefix
            
            itemView.setOnClickListener {
                onItemClick(prefix)
            }
            
            btnDelete.setOnClickListener {
                // Handle delete action
                onItemClick(prefix)
            }
        }
    }
}

class PrefixDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
