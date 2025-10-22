# Shared Shopping Android App

A real-time shared shopping list application built with Kotlin, XML layouts, and Firebase backend.

## Features

- **Real-time Collaboration**: Multiple users can add, update, and mark items as bought in real-time
- **Firebase Integration**: Uses both Firebase Realtime Database and Firestore
- **Offline Support**: Local caching with Room database for offline functionality
- **Email/Password Authentication**: Mandatory sign-in/sign-up system
- **User-Specific Data**: Each user's data is organized under their user ID
- **MVVM Architecture**: Clean architecture with ViewModels, Repository pattern, and LiveData
- **Material Design**: Modern UI with Material Design components

## Firebase Setup Instructions

### 1. Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project"
3. Enter project name: `shared-shopping-app`
4. Enable Google Analytics (optional)
5. Create project

### 2. Add Android App

1. In Firebase Console, click "Add app" and select Android
2. Enter package name: `com.modi.sharedshopping`
3. Enter app nickname: `Shared Shopping`
4. Download `google-services.json` and replace the existing file in `app/google-services.json`

### 3. Enable Firebase Services

#### Firebase Authentication
1. Go to Authentication > Sign-in method
2. Enable "Email/Password" authentication

#### Firebase Realtime Database
1. Go to Realtime Database
2. Click "Create Database"
3. Choose "Start in test mode" (for development)
4. Select a location (e.g., us-central1)

#### Firebase Firestore
1. Go to Firestore Database
2. Click "Create database"
3. Choose "Start in test mode" (for development)
4. Select a location (e.g., us-central1)

### 4. Configure Security Rules

#### Realtime Database Rules
Replace the rules in Firebase Console > Realtime Database > Rules with:

```json
{
  "rules": {
    "users": {
      "$userId": {
        ".read": "auth != null && auth.uid == $userId",
        ".write": "auth != null && auth.uid == $userId",
        "lists": {
          ".read": "auth != null && auth.uid == $userId",
          ".write": "auth != null && auth.uid == $userId",
          "$listId": {
            ".read": "auth != null && auth.uid == $userId",
            ".write": "auth != null && auth.uid == $userId",
            "items": {
              ".read": "auth != null && auth.uid == $userId",
              ".write": "auth != null && auth.uid == $userId"
            }
          }
        }
      }
    }
  }
}
```

#### Firestore Rules
Replace the rules in Firebase Console > Firestore Database > Rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      match /profile {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      match /history/{historyId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

## Project Structure

```
app/
├── src/main/java/com/modi/sharedshopping/
│   ├── data/
│   │   ├── local/           # Room database and DAOs
│   │   ├── model/           # Data models
│   │   └── repository/      # Firebase repository
│   ├── ui/
│   │   ├── adapter/         # RecyclerView adapters
│   │   └── viewmodel/       # ViewModels
│   ├── MainActivity.kt      # Main activity (shopping lists)
│   ├── ListActivity.kt      # List activity (items)
│   ├── ProfileActivity.kt   # Profile activity
│   └── SharedShoppingApplication.kt
├── src/main/res/layout/     # XML layouts
└── google-services.json     # Firebase configuration
```

## Data Structure

### Firebase Realtime Database
```
users/
  {userId}/
    lists/
      {listId}/
        name: "Weekend Groceries"
        ownerId: "uid_123"
        createdAt: 1690000000
        lastModified: 1690000000
        items/
          {itemId}/
            name: "Milk"
            qty: 2
            bought: false
            addedBy: "uid_123"
            timestamp: 1690000000
```

### Firestore
```
users/{uid}/profile/
  name: "john_doe"
  totalItemsAdded: 10
  totalItemsBought: 5
  createdAt: 1690000000

users/{uid}/history/{historyId}/
  listId: "list_123"
  itemName: "Milk"
  action: "added" | "bought" | "unbought" | "deleted"
  timestamp: 1690000000
  userId: "uid_123"
```

## Key Features Implementation

### Real-time Updates
- Uses Firebase Realtime Database listeners for live updates
- Automatically syncs changes between multiple users
- Caches data locally for offline support

### Offline Support
- Room database for local caching
- Automatic sync when network is restored
- Shows cached data when offline

### MVVM Architecture
- Repository pattern for data access
- ViewModels handle business logic
- LiveData for reactive UI updates
- Coroutines for async operations

## Dependencies

- **Firebase**: Authentication, Realtime Database, Firestore
- **Room**: Local database for caching
- **Lifecycle**: ViewModels and LiveData
- **Material Design**: UI components
- **Coroutines**: Async operations
- **Navigation**: Activity navigation

## Building and Running

1. Clone the repository
2. Open in Android Studio
3. Replace `google-services.json` with your Firebase configuration
4. Sync project with Gradle files
5. Run the app on device or emulator

## Testing

The app includes:
- Anonymous authentication for easy testing
- Test mode Firebase rules for development
- Offline functionality testing
- Real-time collaboration testing

## Production Considerations

Before deploying to production:

1. **Security Rules**: Update Firebase rules for production security
2. **Authentication**: Consider implementing proper user authentication
3. **Error Handling**: Add comprehensive error handling
4. **Performance**: Optimize for large datasets
5. **Testing**: Add unit and integration tests
6. **Monitoring**: Set up Firebase Analytics and Crashlytics

## Troubleshooting

### Common Issues

1. **Build Errors**: Ensure `google-services.json` is properly placed
2. **Firebase Connection**: Check internet connection and Firebase project setup
3. **Authentication**: Verify anonymous authentication is enabled
4. **Database Rules**: Ensure security rules allow read/write operations

### Debug Mode

The app includes debug logging for Firebase operations. Check Logcat for detailed error messages.

## License

This project is for educational purposes. Feel free to use and modify as needed.
