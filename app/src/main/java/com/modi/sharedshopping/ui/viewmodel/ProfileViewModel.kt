package com.modi.sharedshopping.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modi.sharedshopping.data.model.HistoryItem
import com.modi.sharedshopping.data.model.UserProfile
import com.modi.sharedshopping.data.repository.ShoppingRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ShoppingRepository) : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _historyItems = MutableLiveData<List<HistoryItem>>()
    val historyItems: LiveData<List<HistoryItem>> = _historyItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        loadUserProfile()
        loadUserHistory()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getUserProfile()
                if (result.isSuccess) {
                    _userProfile.value = result.getOrNull()
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to load profile"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }

    fun loadUserHistory() {
        viewModelScope.launch {
            try {
                repository.getUserHistory().collect { history ->
                    _historyItems.value = history
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
