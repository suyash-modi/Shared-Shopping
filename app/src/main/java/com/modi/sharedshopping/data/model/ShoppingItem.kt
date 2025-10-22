package com.modi.sharedshopping.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey
    val id: String,
    val listId: String,
    val name: String,
    val quantity: Int = 1,
    val bought: Boolean = false,
    val addedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

