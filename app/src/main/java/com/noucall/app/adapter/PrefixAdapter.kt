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
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)

        fun bind(blockedPrefix: BlockedPrefix) {
            // Format prefix for display (add space after first 2 digits for readability)
            val formattedPrefix = formatPrefixForDisplay(blockedPrefix.prefix)
            tvPrefix.text = "$formattedPrefix - ${blockedPrefix.comment}"
            
            // Only the edit button is clickable, not the whole item
            btnEdit.setOnClickListener {
                onItemClick(blockedPrefix)
            }
        }
        
        private fun formatPrefixForDisplay(prefix: String): String {
            // Add space after first 2 digits for readability (e.g., "0948" -> "09 48")
            return if (prefix.length >= 2) {
                prefix.substring(0, 2) + " " + prefix.substring(2)
            } else {
                prefix
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
