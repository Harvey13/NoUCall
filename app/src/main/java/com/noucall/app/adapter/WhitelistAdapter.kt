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
import com.noucall.app.data.CountryData

class WhitelistAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<String, WhitelistAdapter.WhitelistViewHolder>(WhitelistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhitelistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_whitelist, parent, false)
        return WhitelistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WhitelistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WhitelistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCountry: TextView = itemView.findViewById(R.id.tv_country)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_edit)

        fun bind(countryPrefix: String) {
            // Get localized country name from prefix
            val context = itemView.context
            val country = CountryData.findCountryByPrefix(context, countryPrefix)
            val displayName = if (country != null) {
                "${country.prefix} ${country.name}"
            } else {
                countryPrefix // Fallback to prefix if country not found
            }
            
            tvCountry.text = displayName
            
            // Only the delete button is clickable, not the whole item
            btnDelete.setOnClickListener {
                onItemClick(countryPrefix)
            }
        }
    }
}

class WhitelistDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
