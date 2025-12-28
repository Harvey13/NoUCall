package com.noucall.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noucall.app.R
import com.noucall.app.utils.BlockedCall
import java.text.SimpleDateFormat
import java.util.*

class BlockedCallAdapter : ListAdapter<BlockedCall, BlockedCallAdapter.BlockedCallViewHolder>(BlockedCallDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedCallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_call, parent, false)
        return BlockedCallViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedCallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BlockedCallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)

        fun bind(blockedCall: BlockedCall) {
            tvPhoneNumber.text = blockedCall.phoneNumber

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(blockedCall.timestamp))
        }
    }
}

class BlockedCallDiffCallback : DiffUtil.ItemCallback<BlockedCall>() {
    override fun areItemsTheSame(oldItem: BlockedCall, newItem: BlockedCall): Boolean {
        return oldItem.phoneNumber == newItem.phoneNumber && oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: BlockedCall, newItem: BlockedCall): Boolean {
        return oldItem == newItem
    }
}
