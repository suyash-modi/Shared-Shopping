package com.modi.sharedshopping.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modi.sharedshopping.data.model.ShoppingItem
import com.modi.sharedshopping.data.repository.ShoppingRepository
import kotlinx.coroutines.launch

class ListViewModel(private val repository: ShoppingRepository) : ViewModel() {

    private val _shoppingItems = MutableLiveData<List<ShoppingItem>>()
    val shoppingItems: LiveData<List<ShoppingItem>> = _shoppingItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var currentListId: String? = null

    fun loadItemsForList(listId: String) {
        currentListId = listId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getItemsByListId(listId).collect { items ->
                    _shoppingItems.value = items
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }

    fun addItemToList(itemName: String, quantity: Int) {
        val listId = currentListId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.addItemToList(listId, itemName, quantity)
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to add item"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
                _isLoading.value = false
            }
        }
    }

    fun toggleItemBought(itemId: String, bought: Boolean) {
        val listId = currentListId ?: return
        viewModelScope.launch {
            try {
                val result = repository.toggleItemBought(listId, itemId, bought)
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to update item"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error occurred"
            }
        }
    }

    fun deleteItem(itemId: String) {
        val listId = currentListId ?: return
        viewModelScope.launch {
            try {
                val result = repository.deleteItem(listId, itemId)
                if (result.isFailure) {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to delete item"
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

