package com.modi.sharedshopping.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.modi.sharedshopping.data.model.HistoryItem
import com.modi.sharedshopping.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : ListAdapter<HistoryItem, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(historyItem: HistoryItem) {
            binding.itemNameText.text = historyItem.itemName
            
            // Set action text and color
            when (historyItem.action) {
                "added" -> {
                    binding.actionText.text = "Added"
                    binding.actionText.setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
                }
                "bought" -> {
                    binding.actionText.text = "Bought"
                    binding.actionText.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                }
                "unbought" -> {
                    binding.actionText.text = "Unbought"
                    binding.actionText.setBackgroundColor(android.graphics.Color.parseColor("#FF9800"))
                }
                "deleted" -> {
                    binding.actionText.text = "Deleted"
                    binding.actionText.setBackgroundColor(android.graphics.Color.parseColor("#F44336"))
                }
                else -> {
                    binding.actionText.text = historyItem.action
                    binding.actionText.setBackgroundColor(android.graphics.Color.parseColor("#9E9E9E"))
                }
            }

            // Format timestamp
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            binding.timestampText.text = dateFormat.format(Date(historyItem.timestamp))
        }
    }

    class HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
