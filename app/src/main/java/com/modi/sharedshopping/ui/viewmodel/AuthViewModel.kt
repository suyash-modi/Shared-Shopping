package com.modi.sharedshopping.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.modi.sharedshopping.data.repository.ShoppingRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: ShoppingRepository) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isSignedIn = MutableLiveData<Boolean>()
    val isSignedIn: LiveData<Boolean> = _isSignedIn

    private val auth = FirebaseAuth.getInstance()

    fun signIn(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Initialize user profile if it doesn't exist
                        initializeUserProfile(user.uid, user.email ?: "Unknown")
                        _isSignedIn.value = true
                    }
                } else {
                    _errorMessage.value = task.exception?.message ?: "Sign in failed"
                }
            }
    }

    fun signUp(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Initialize user profile for new user
                        initializeUserProfile(user.uid, user.email ?: "Unknown")
                        _isSignedIn.value = true
                    }
                } else {
                    _errorMessage.value = task.exception?.message ?: "Sign up failed"
                }
            }
    }

    private fun initializeUserProfile(uid: String, email: String) {
        viewModelScope.launch {
            try {
                // Create user profile in Firestore
                repository.initializeUserProfile(uid, email)
            } catch (e: Exception) {
                // Log error but don't block sign-in
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _isSignedIn.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
