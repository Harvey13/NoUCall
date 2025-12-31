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
import com.noucall.app.utils.BlockedSms
import java.text.SimpleDateFormat
import java.util.*

class BlockedSmsAdapter(
    private val onEditClick: (BlockedSms) -> Unit
) : ListAdapter<BlockedSms, BlockedSmsAdapter.BlockedSmsViewHolder>(BlockedSmsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedSmsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_sms, parent, false)
        return BlockedSmsViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedSmsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BlockedSmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPhoneNumber: TextView = itemView.findViewById(R.id.tv_phone_number)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_message)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit)

        fun bind(blockedSms: BlockedSms) {
            tvPhoneNumber.text = blockedSms.phoneNumber
            tvMessage.text = blockedSms.message

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvTimestamp.text = sdf.format(Date(blockedSms.timestamp))
            
            // Only the edit button is clickable, not the whole item
            btnEdit.setOnClickListener {
                onEditClick(blockedSms)
            }
        }
    }
}

class BlockedSmsDiffCallback : DiffUtil.ItemCallback<BlockedSms>() {
    override fun areItemsTheSame(oldItem: BlockedSms, newItem: BlockedSms): Boolean {
        return oldItem.phoneNumber == newItem.phoneNumber && oldItem.timestamp == newItem.timestamp
    }

    override fun areContentsTheSame(oldItem: BlockedSms, newItem: BlockedSms): Boolean {
        return oldItem == newItem
    }
}
