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
import com.noucall.app.data.BlockedPrefix

class PrefixAdapter(
    private val onItemClick: (BlockedPrefix) -> Unit
) : ListAdapter<BlockedPrefix, PrefixAdapter.PrefixViewHolder>(PrefixDiffCallback()) {

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

        fun bind(blockedPrefix: BlockedPrefix) {
            tvPrefix.text = "${blockedPrefix.prefix} - ${blockedPrefix.comment}"
            
            itemView.setOnClickListener {
                onItemClick(blockedPrefix)
            }
            
            btnDelete.setOnClickListener {
                // Handle delete action
                onItemClick(blockedPrefix)
            }
        }
    }
}

class PrefixDiffCallback : DiffUtil.ItemCallback<BlockedPrefix>() {
    override fun areItemsTheSame(oldItem: BlockedPrefix, newItem: BlockedPrefix): Boolean {
        return oldItem.prefix == newItem.prefix
    }

    override fun areContentsTheSame(oldItem: BlockedPrefix, newItem: BlockedPrefix): Boolean {
        return oldItem == newItem
    }
}
