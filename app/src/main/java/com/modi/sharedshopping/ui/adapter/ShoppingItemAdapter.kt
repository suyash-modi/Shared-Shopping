package com.modi.sharedshopping.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.modi.sharedshopping.data.model.ShoppingItem
import com.modi.sharedshopping.databinding.ItemShoppingItemBinding

class ShoppingItemAdapter(
    private val onItemToggle: (ShoppingItem, Boolean) -> Unit,
    private val onItemDelete: (ShoppingItem) -> Unit
) : ListAdapter<ShoppingItem, ShoppingItemAdapter.ShoppingItemViewHolder>(ShoppingItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingItemViewHolder {
        val binding = ItemShoppingItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShoppingItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShoppingItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ShoppingItemViewHolder(
        private val binding: ItemShoppingItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shoppingItem: ShoppingItem) {
            binding.itemNameText.text = shoppingItem.name
            binding.quantityText.text = shoppingItem.quantity.toString()
            binding.checkBoxBought.isChecked = shoppingItem.bought

            // Update text appearance based on bought status
            if (shoppingItem.bought) {
                binding.itemNameText.alpha = 0.6f
                binding.quantityText.alpha = 0.6f
            } else {
                binding.itemNameText.alpha = 1.0f
                binding.quantityText.alpha = 1.0f
            }

            binding.checkBoxBought.setOnCheckedChangeListener { _, isChecked ->
                onItemToggle(shoppingItem, isChecked)
            }

            binding.deleteButton.setOnClickListener {
                onItemDelete(shoppingItem)
            }
        }
    }

    class ShoppingItemDiffCallback : DiffUtil.ItemCallback<ShoppingItem>() {
        override fun areItemsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
            return oldItem == newItem
        }
    }
}

