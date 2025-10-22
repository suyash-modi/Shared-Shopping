package com.modi.sharedshopping.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.modi.sharedshopping.data.local.ShoppingDatabase
import com.modi.sharedshopping.data.local.ShoppingItemDao
import com.modi.sharedshopping.data.local.ShoppingListDao
import com.modi.sharedshopping.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ShoppingRepository(
    private val database: ShoppingDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val firestore: FirebaseFirestore
) {
    private val shoppingListDao: ShoppingListDao = database.shoppingListDao()
    private val shoppingItemDao: ShoppingItemDao = database.shoppingItemDao()

    // Firebase Realtime Database operations - USER SPECIFIC
    fun getAllLists(): Flow<List<ShoppingList>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listsRef = firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ShoppingList>()
                
                // Handle case when snapshot doesn't exist or is empty
                if (!snapshot.exists()) {
                    trySend(emptyList())
                    return
                }
                
                for (childSnapshot in snapshot.children) {
                    try {
                        val listData = childSnapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                        listData?.let { data ->
                            val list = ShoppingList(
                                id = childSnapshot.key ?: "",
                                name = data["name"] as? String ?: "",
                                ownerId = data["ownerId"] as? String ?: currentUser.uid,
                                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                                lastModified = (data["lastModified"] as? Long) ?: System.currentTimeMillis()
                            )
                            lists.add(list)
                            
                            // Cache locally
                            kotlinx.coroutines.runBlocking {
                                shoppingListDao.insertList(list)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseError", "Error parsing list data: ${e.message}")
                    }
                }
                trySend(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        listsRef.addValueEventListener(listener)
        awaitClose { listsRef.removeEventListener(listener) }
    }

    fun getItemsByListId(listId: String): Flow<List<ShoppingItem>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val itemsRef = firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("items")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<ShoppingItem>()
                
                // Handle case when snapshot doesn't exist or is empty
                if (!snapshot.exists()) {
                    trySend(emptyList())
                    return
                }
                
                for (childSnapshot in snapshot.children) {
                    try {
                        val itemData = childSnapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                        itemData?.let { data ->
                            val item = ShoppingItem(
                                id = childSnapshot.key ?: "",
                                listId = listId,
                                name = data["name"] as? String ?: "",
                                quantity = (data["qty"] as? Long)?.toInt() ?: 1,
                                bought = data["bought"] as? Boolean ?: false,
                                addedBy = data["addedBy"] as? String ?: "",
                                timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis()
                            )
                            items.add(item)
                            
                            // Cache locally
                            kotlinx.coroutines.runBlocking {
                                shoppingItemDao.insertItem(item)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseError", "Error parsing item data: ${e.message}")
                    }
                }
                trySend(items)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        itemsRef.addValueEventListener(listener)
        awaitClose { itemsRef.removeEventListener(listener) }
    }

    suspend fun createList(name: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            val listId = firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").push().key ?: ""
            val list = ShoppingList(
                id = listId,
                name = name,
                ownerId = currentUser.uid,
                createdAt = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis()
            )

            val listData = mapOf(
                "name" to name,
                "ownerId" to currentUser.uid,
                "createdAt" to list.createdAt,
                "lastModified" to list.lastModified
            )

            firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).setValue(listData).await()
            
            // Cache locally
            shoppingListDao.insertList(list)
            
            Result.success(listId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addItemToList(listId: String, itemName: String, quantity: Int = 1): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            val itemId = firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("items").push().key ?: ""
            val timestamp = System.currentTimeMillis()
            
            val itemData = mapOf(
                "name" to itemName,
                "qty" to quantity,
                "bought" to false,
                "addedBy" to currentUser.uid,
                "timestamp" to timestamp
            )

            firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("items").child(itemId).setValue(itemData).await()
            
            // Update list lastModified
            firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("lastModified").setValue(timestamp).await()
            
            val item = ShoppingItem(
                id = itemId,
                listId = listId,
                name = itemName,
                quantity = quantity,
                bought = false,
                addedBy = currentUser.uid,
                timestamp = timestamp
            )
            
            // Cache locally
            shoppingItemDao.insertItem(item)
            
            // Log to Firestore
            logToHistory(listId, itemName, "added", currentUser.uid)
            
            Result.success(itemId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleItemBought(listId: String, itemId: String, bought: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("items").child(itemId).child("bought").setValue(bought).await()
            
            // Update list lastModified
            firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("lastModified").setValue(System.currentTimeMillis()).await()
            
            // Update local cache
            val item = shoppingItemDao.getItemById(itemId)
            item?.let {
                val updatedItem = it.copy(bought = bought)
                shoppingItemDao.updateItem(updatedItem)
            }
            
            // Log to Firestore
            val itemName = item?.name ?: "Unknown Item"
            val action = if (bought) "bought" else "unbought"
            logToHistory(listId, itemName, action, currentUser.uid)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(listId: String, itemId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("items").child(itemId).removeValue().await()
            
            // Update list lastModified
            firebaseDatabase.reference.child("users").child(currentUser.uid).child("lists").child(listId).child("lastModified").setValue(System.currentTimeMillis()).await()
            
            // Update local cache
            val item = shoppingItemDao.getItemById(itemId)
            shoppingItemDao.deleteItemById(itemId)
            
            // Log to Firestore
            val itemName = item?.name ?: "Unknown Item"
            logToHistory(listId, itemName, "deleted", currentUser.uid)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Initialize user profile for new users
    suspend fun initializeUserProfile(uid: String, email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = UserProfile(
                uid = uid,
                name = email.split("@")[0], // Use email prefix as name
                totalItemsAdded = 0,
                totalItemsBought = 0,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("users").document(uid).collection("profile").document("data").set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firestore operations for user profiles and history
    suspend fun getUserProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            val document = firestore.collection("users").document(currentUser.uid).collection("profile").document("data").get().await()
            
            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java) ?: UserProfile(currentUser.uid)
                // Ensure the profile has the correct uid
                val profileWithUid = if (profile.uid.isEmpty()) profile.copy(uid = currentUser.uid) else profile
                Result.success(profileWithUid)
            } else {
                // Create new profile
                val profile = UserProfile(currentUser.uid)
                firestore.collection("users").document(currentUser.uid).collection("profile").document("data").set(profile).await()
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                return@withContext Result.failure(Exception("User not authenticated"))
            }

            firestore.collection("users").document(currentUser.uid).collection("profile").document("data").set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserHistory(): Flow<List<HistoryItem>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val historyRef = firestore.collection("users").document(currentUser.uid).collection("history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)

        val listener = historyRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val historyItems = snapshot?.documents?.mapNotNull { document ->
                try {
                    val historyItem = document.toObject(HistoryItem::class.java)
                    historyItem?.copy(id = document.id)
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseError", "Error parsing history item: ${e.message}")
                    null
                }
            } ?: emptyList()

            trySend(historyItems)
        }

        awaitClose { listener.remove() }
    }

    private suspend fun logToHistory(listId: String, itemName: String, action: String, userId: String) {
        try {
            val historyItem = HistoryItem(
                listId = listId,
                itemName = itemName,
                action = action,
                timestamp = System.currentTimeMillis(),
                userId = userId
            )

            firestore.collection("users").document(userId).collection("history").add(historyItem).await()
            
            // Update user profile stats
            val profileResult = getUserProfile()
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()!!
                val updatedProfile = when (action) {
                    "added" -> profile.copy(totalItemsAdded = profile.totalItemsAdded + 1)
                    "bought" -> profile.copy(totalItemsBought = profile.totalItemsBought + 1)
                    "unbought" -> profile.copy(totalItemsBought = maxOf(0, profile.totalItemsBought - 1))
                    else -> profile
                }
                updateUserProfile(updatedProfile)
            }
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            e.printStackTrace()
        }
    }

    // Debug method to test Firebase connection
    fun testFirebaseConnection(): Flow<String> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend("No user authenticated")
            close()
            return@callbackFlow
        }

        trySend("User authenticated: ${currentUser.uid}")
        
        // Test Realtime Database connection
        val testRef = firebaseDatabase.reference.child("test").child("connection")
        testRef.setValue("test_value")
            .addOnSuccessListener {
                trySend("Realtime Database: Connected")
            }
            .addOnFailureListener { e ->
                trySend("Realtime Database: Error - ${e.message}")
            }

        // Test Firestore connection
        firestore.collection("test").document("connection").set(mapOf("status" to "connected"))
            .addOnSuccessListener {
                trySend("Firestore: Connected")
            }
            .addOnFailureListener { e ->
                trySend("Firestore: Error - ${e.message}")
            }

        awaitClose { }
    }

    // Local cache operations
    fun getCachedLists(): Flow<List<ShoppingList>> = shoppingListDao.getAllLists()
    fun getCachedItems(listId: String): Flow<List<ShoppingItem>> = shoppingItemDao.getItemsByListId(listId)
}
