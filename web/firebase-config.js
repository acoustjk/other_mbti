/**
 * Firebase Configuration & Helper Services
 * '타인이 보는 나의 MBTI' - Firestore & Auth Manager
 */

// Firebase Configuration (Real Project Settings)
const firebaseConfig = {
  apiKey: "AIzaSyDnWB6QLfB3AaDQzVTJ0LH6EY6cf_F6jjU",
  authDomain: "othermbti.firebaseapp.com",
  projectId: "othermbti",
  storageBucket: "othermbti.appspot.com",
  messagingSenderId: "763201396428",
  appId: "1:763201396428:web:othermbti"
};

// Firestore Collection References (Firestore SDK standard)
const COLLECTIONS = {
  USERS: 'users',
  EVALUATIONS: 'evaluations'
};

// Helper flag to check if Firebase is configured with real credentials
function isFirebaseConfigured() {
  return firebaseConfig.apiKey && firebaseConfig.apiKey !== "YOUR_FIREBASE_API_KEY";
}

// Export references for app usage
window.FirebaseService = {
  config: firebaseConfig,
  collections: COLLECTIONS,
  isConfigured: isFirebaseConfigured
};
