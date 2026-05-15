# Namma-Shaale Inventory App
**Android App Development using GenAI — Project #59**

---

##  Project Overview
Namma-Shaale Inventory App is an Android-based digital asset management system designed for schools and educational institutions. The application helps schools efficiently track, manage, and audit assets such as laptops, projectors, laboratory equipment, desks, chairs, and other inventory items.

The app replaces traditional paper-based inventory management with a centralized digital solution that improves accuracy, reduces manual effort, and simplifies auditing processes.

---

##  Problem Statement
Many schools still rely on manual registers or spreadsheets to maintain inventory records. This often leads to:
- Misplaced or inaccurate records
- Difficulty tracking damaged or unavailable assets
- Delayed auditing and reporting
- Time-consuming inventory management

The Namma-Shaale Inventory App solves these issues by providing a secure and organized digital inventory system with real-time updates and easy asset tracking.

---

## ✅ Open in Android Studio — Exact Steps

### 1. Extract ZIP
Right-click `NammaShaaleApp.zip` → **Extract All** → you get `NammaShaaleApp/` folder

### 2. Open in Android Studio
**File → Open** → select the `NammaShaaleApp/` folder → **OK**

### 3. Gradle Sync
Android Studio auto-syncs. Wait for **"Gradle sync finished"** at the bottom bar.
First run takes 3–5 min (downloads dependencies). Do NOT click anything during sync.

### 4. Add Your Firebase File ⚠️ REQUIRED
- [console.firebase.google.com](https://console.firebase.google.com) → Add Project → `NammaShaale`
- Add Android App → Package name: `com.nammashaale.app`
- Download `google-services.json`
- In Android Studio Project panel → switch to **"Android" view**
- Right-click `app` → **Show in Explorer** → replace existing `google-services.json`
- In Firebase Console enable: **Auth (Email/Password)** + **Firestore** + **Storage**

### 5. Run on Pixel 7 API 34
- **Tools → Device Manager → Create Device → Pixel 7 → API 34 → Finish** [This is optional ie can select according to our wish]
- Press **▶ Run**
- Login screen appears → Register → Start adding assets!

---

## Navigation Map
```
Login → Dashboard → Asset List → Asset Detail
              ↓
         Add Asset (FAB)
         Health Check (top bar) → Reports
         Bottom Nav: Home | Assets | Repairs | Reports
```

## Features
- Firebase Auth (login/register)
- Dashboard with stats (Working / Needs Check / Needs Repair)
- Add Asset with CameraX photo (runtime permission safe on API 34)
- Monthly Health Check — 3-tap condition update
- Repair List for SDMC
- Shareable Summary Report
- Search + filter assets
- Offline-first (Room DB) + real-time Firebase sync

## Technologies Used
- Kotlin
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore
- Room Database
- MVVM Architecture
- Material Design 3
- Gradle
- Android Studio
- Coroutines & Flow

---

## Setup Instructions

### Prerequisites
Make sure you have:
- Android Studio installed
- Android SDK installed
- Internet connection for Gradle sync

---

## Common Fixes
| Problem | Fix |
|---|---|
| Gradle sync fails | File → Invalidate Caches → Restart |
| `google-services.json` error | Replace with your real Firebase file |
| SDK not found | File → Project Structure → SDK Location |
| Camera error on emulator | Use physical device or enable camera in AVD |


Some notes for Firebase

Configure Firebase
Go to Firebase Console
Create a Firebase project

Add Android app with package name:
com.nammashaale.app

Download google-services.json
Place the file inside:
app/google-services.json

Sync Gradle

Click:
Sync Now

Connect an Android device or start an emulator
Click Run in Android Studio

Project Structure:

app/
 ├── ui/
 ├── database/
 ├── repository/
 ├── viewmodel/
 ├── firebase/
 ├── screens/
 └── utils/

