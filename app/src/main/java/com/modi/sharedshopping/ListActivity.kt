package com.modi.sharedshopping

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.modi.sharedshopping.data.model.ShoppingItem
import com.modi.sharedshopping.databinding.ActivityListBinding
import com.modi.sharedshopping.databinding.DialogAddItemBinding
import com.modi.sharedshopping.ui.adapter.ShoppingItemAdapter
import com.modi.sharedshopping.ui.viewmodel.ListViewModel
import com.modi.sharedshopping.ui.viewmodel.ViewModelFactory

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var viewModel: ListViewModel
    private lateinit var adapter: ShoppingItemAdapter
    private var listId: String? = null
    private var listName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listId = intent.getStringExtra("listId")
        listName = intent.getStringExtra("listName")

        if (listId == null) {
            Toast.makeText(this, "Invalid list", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadItems()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupViewModel() {
        val repository = (application as SharedShoppingApplication).repository
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ListViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ShoppingItemAdapter(
            onItemToggle = { item, bought ->
                viewModel.toggleItemBought(item.id, bought)
            },
            onItemDelete = { item ->
                showDeleteConfirmation(item)
            }
        )
        binding.recyclerViewItems.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewItems.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.fabAddItem.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.shoppingItems.observe(this) { items ->
            adapter.submitList(items)
            updateEmptyState(items.isEmpty())
            updateItemCount(items.size)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun loadItems() {
        listId?.let { id ->
            viewModel.loadItemsForList(id)
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateItemCount(count: Int) {
        binding.itemCountText.text = "$count item${if (count != 1) "s" else ""}"
    }

    private fun showAddItemDialog() {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Add New Item")
            .setPositiveButton("Add") { _, _ ->
                val itemName = dialogBinding.editTextItemName.text.toString().trim()
                val quantityText = dialogBinding.editTextQuantity.text.toString().trim()
                val quantity = quantityText.toIntOrNull() ?: 1
                
                if (itemName.isNotEmpty()) {
                    viewModel.addItemToList(itemName, quantity)
                } else {
                    Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun showDeleteConfirmation(item: ShoppingItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete \"${item.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteItem(item.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        listName?.let { name ->
            binding.toolbar.title = name
        }
    }
}
