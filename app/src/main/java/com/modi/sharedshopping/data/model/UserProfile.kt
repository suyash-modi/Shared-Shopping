package com.modi.sharedshopping.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "Anonymous User",
    val totalItemsAdded: Int = 0,
    val totalItemsBought: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "Anonymous User", 0, 0, System.currentTimeMillis())
}
