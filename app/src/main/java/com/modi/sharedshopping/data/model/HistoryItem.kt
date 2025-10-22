package com.modi.sharedshopping.data.model

data class HistoryItem(
    val id: String = "",
    val listId: String = "",
    val itemName: String = "",
    val action: String = "", // "added", "bought", "unbought", "deleted"
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", System.currentTimeMillis(), "")
}
