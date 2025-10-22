package com.modi.sharedshopping

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.modi.sharedshopping.data.model.ShoppingList
import com.modi.sharedshopping.databinding.ActivityMainBinding
import com.modi.sharedshopping.databinding.DialogCreateListBinding
import com.modi.sharedshopping.ui.adapter.ShoppingListAdapter
import com.modi.sharedshopping.ui.viewmodel.MainViewModel
import com.modi.sharedshopping.ui.viewmodel.ViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ShoppingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
    }

    private fun setupViewModel() {
        val repository = (application as SharedShoppingApplication).repository
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ShoppingListAdapter { shoppingList ->
            openShoppingList(shoppingList)
        }
        binding.recyclerViewLists.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewLists.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.fabCreateList.setOnClickListener {
            showCreateListDialog()
        }

        binding.fabProfile.setOnClickListener {
            openProfile()
        }
    }

    private fun observeViewModel() {
        viewModel.shoppingLists.observe(this) { lists ->
            adapter.submitList(lists)
            updateEmptyState(lists.isEmpty())
            updateListCount(lists.size)
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

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewLists.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateListCount(count: Int) {
        binding.listCountText.text = "$count list${if (count != 1) "s" else ""}"
    }

    private fun showCreateListDialog() {
        val dialogBinding = DialogCreateListBinding.inflate(layoutInflater)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setTitle("Create New Shopping List")
            .setPositiveButton("Create") { _, _ ->
                val listName = dialogBinding.editTextListName.text.toString().trim()
                if (listName.isNotEmpty()) {
                    viewModel.createNewList(listName)
                } else {
                    Toast.makeText(this, "Please enter a list name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun openShoppingList(shoppingList: ShoppingList) {
        val intent = Intent(this, ListActivity::class.java)
        intent.putExtra("listId", shoppingList.id)
        intent.putExtra("listName", shoppingList.name)
        startActivity(intent)
    }

    private fun openProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
}