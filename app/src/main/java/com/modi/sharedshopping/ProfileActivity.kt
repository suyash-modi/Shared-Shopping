package com.modi.sharedshopping

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.modi.sharedshopping.databinding.ActivityProfileBinding
import com.modi.sharedshopping.ui.adapter.HistoryAdapter
import com.modi.sharedshopping.ui.viewmodel.ProfileViewModel
import com.modi.sharedshopping.ui.viewmodel.ViewModelFactory

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
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

    private fun setupClickListeners() {
        binding.buttonSignOut.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun setupViewModel() {
        val repository = (application as SharedShoppingApplication).repository
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewHistory.adapter = historyAdapter
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                binding.userNameText.text = it.name
                // Get email from Firebase Auth instead of showing UID
                val currentUser = FirebaseAuth.getInstance().currentUser
                binding.userEmailText.text = currentUser?.email ?: "No email"
                binding.totalItemsAddedText.text = it.totalItemsAdded.toString()
                binding.totalItemsBoughtText.text = it.totalItemsBought.toString()
            }
        }

        viewModel.historyItems.observe(this) { history ->
            historyAdapter.submitList(history)
            updateEmptyHistoryState(history.isEmpty())
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

    private fun updateEmptyHistoryState(isEmpty: Boolean) {
        binding.emptyHistoryLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signOut() {
        val authViewModel = ViewModelProvider(this, ViewModelFactory((application as SharedShoppingApplication).repository))[com.modi.sharedshopping.ui.viewmodel.AuthViewModel::class.java]
        authViewModel.signOut()
        
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
