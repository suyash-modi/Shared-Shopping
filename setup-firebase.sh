#!/bin/bash

# Shared Shopping App - Firebase Setup Script
# This script helps set up Firebase for the Shared Shopping Android app

echo "🔥 Shared Shopping App - Firebase Setup"
echo "======================================"
echo ""

echo "📋 Prerequisites:"
echo "1. Firebase project created at https://console.firebase.google.com/"
echo "2. Android app added to Firebase project"
echo "3. google-services.json downloaded and placed in app/ directory"
echo ""

echo "🔧 Firebase Services to Enable:"
echo "1. Authentication (Anonymous sign-in)"
echo "2. Realtime Database (Test mode)"
echo "3. Firestore Database (Test mode)"
echo ""

echo "📁 Project Structure:"
echo "app/"
echo "├── google-services.json (REQUIRED - Download from Firebase Console)"
echo "├── src/main/java/com/modi/sharedshopping/"
echo "│   ├── data/"
echo "│   ├── ui/"
echo "│   └── *.kt files"
echo "└── src/main/res/layout/"
echo ""

echo "🚀 Next Steps:"
echo "1. Replace app/google-services.json with your Firebase configuration"
echo "2. Enable Firebase services in Firebase Console"
echo "3. Update security rules (see README.md)"
echo "4. Build and run the app in Android Studio"
echo ""

echo "📖 For detailed instructions, see README.md"
echo ""

echo "✅ Setup complete! Happy coding!"

