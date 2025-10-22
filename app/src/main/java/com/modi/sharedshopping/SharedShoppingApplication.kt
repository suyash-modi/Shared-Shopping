package com.modi.sharedshopping

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.modi.sharedshopping.data.local.ShoppingDatabase
import com.modi.sharedshopping.data.repository.ShoppingRepository

class SharedShoppingApplication : Application() {

    lateinit var repository: ShoppingRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Enable offline persistence for Firebase Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        
        // Enable offline persistence for Firestore
        FirebaseFirestore.getInstance().enableNetwork()
        
        // Initialize Room database
        val database = ShoppingDatabase.getDatabase(this)
        
        // Initialize repository
        repository = ShoppingRepository(
            database = database,
            firebaseAuth = FirebaseAuth.getInstance(),
            firebaseDatabase = FirebaseDatabase.getInstance(),
            firestore = FirebaseFirestore.getInstance()
        )
    }
}
