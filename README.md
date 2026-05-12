# Namma-Shaale Inventory App
**Android App Development using GenAI — Project #59**

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
- **Tools → Device Manager → Create Device → Pixel 7 → API 34 → Finish**
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

## Common Fixes
| Problem | Fix |
|---|---|
| Gradle sync fails | File → Invalidate Caches → Restart |
| `google-services.json` error | Replace with your real Firebase file |
| SDK not found | File → Project Structure → SDK Location |
| Camera error on emulator | Use physical device or enable camera in AVD |
