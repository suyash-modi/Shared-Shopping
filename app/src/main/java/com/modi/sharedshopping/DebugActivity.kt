package com.modi.sharedshopping

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.modi.sharedshopping.databinding.ActivityDebugBinding
import com.modi.sharedshopping.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class DebugActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebugBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.buttonTestConnection.setOnClickListener {
            testFirebaseConnection()
        }
    }

    private fun testFirebaseConnection() {
        val repository = (application as SharedShoppingApplication).repository
        
        binding.textDebugOutput.text = "Testing Firebase connection...\n"
        
        lifecycleScope.launch {
            repository.testFirebaseConnection().collect { message ->
                runOnUiThread {
                    binding.textDebugOutput.append("$message\n")
                }
            }
        }
    }
}
