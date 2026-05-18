# 🚀 RUN AEGIS - VISUAL GUIDE

## Quick Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    15-MINUTE SETUP                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ① Android Studio          2 min                           │
│  ② Firebase Setup          5 min                           │
│  ③ Clone Project           2 min                           │
│  ④ Build APK               3 min                           │
│  ⑤ Run on Emulator         3 min                           │
│                                                             │
│  ✅ Total: 15 minutes                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 📥 Installation Steps

### STEP 1: Android Studio

```bash
1. Download from: https://developer.android.com/studio
2. Install and run
3. Follow setup wizard
4. Install Android SDK + Emulator
```

**Status**: ✅ Ready when Android Studio opens

---

### STEP 2: Firebase Setup

```
FIREBASE CONSOLE
├── Create new project
│   └── Name: "aegis-demo"
│
├── Add Android app
│   ├── Package: com.karthik.aegis
│   └── Download google-services.json
│
├── Enable Authentication
│   ├── Email/Password
│   └── Phone
│
├── Enable Realtime Database
│   ├── Create database
│   └── Test mode
│
├── Enable Cloud Firestore
│   ├── Create database
│   └── Test mode
│
└── Enable Cloud Messaging
    └── Note Server API Key
```

**Status**: ✅ Ready when all services enabled

---

### STEP 3: Clone Project

```bash
# Option A: Git
$ git clone https://github.com/MKarthik730/aegis.git
$ cd aegis

# Option B: Direct folder
$ cd /path/to/aegis
```

**Status**: ✅ Ready when folder has all files

---

### STEP 4: Add Firebase Config

```
Copy google-services.json to:

aegis/
└── app/
    └── google-services.json  ← Place file here
```

**Status**: ✅ Ready when file is in place

---

### STEP 5: Configure Maps API

```
Edit gradle.properties:

MAPS_API_KEY=your_actual_api_key_here
```

Get API key from Google Cloud Console:
1. Select Firebase project
2. Enable "Maps SDK for Android"
3. Create API Key
4. Paste above

**Status**: ✅ Ready when key is added

---

### STEP 6: Open in Android Studio

```
1. Android Studio → File → Open
2. Select aegis folder
3. Click Open
4. Wait for Gradle sync (2-3 min)
5. Should see ✅ "Gradle build finished"
```

**Status**: ✅ Ready when Gradle sync completes

---

### STEP 7: Build APK

```
Method A: Android Studio UI
└── Build → Build Bundle(s)/APK(s) → Build APK(s)
    └── Wait 3-5 minutes
    └── See: "Build completed successfully"

Method B: Command Line
└── ./gradlew assembleDebug
    └── Wait 3-5 minutes
    └── See: "BUILD SUCCESSFUL"
```

**Status**: ✅ Ready when APK is built

---

### STEP 8: Launch Emulator

```
1. Android Studio → Device Manager
2. Create Device
   ├── Model: Pixel 4a
   └── API: 34
3. Click Play (▶️) button
4. Wait for emulator to boot (1-2 min)
```

**Status**: ✅ Ready when Android home screen shows

---

### STEP 9: Run App

```
1. Android Studio → Run (▶️ green button)
2. Select emulator
3. App installs and launches
4. Splash screen appears (2 seconds)
5. Auth screen shows
```

**Status**: ✅ Ready when app launches

---

### STEP 10: First Launch

```
SPLASH SCREEN (2 seconds)
          ↓
AUTH SCREEN
├── Click "Sign Up"
├── Enter:
│   ├── Name: Test User
│   ├── Email: test@aegis.app
│   └── Password: testpassword123
└── Click "Create Account"
          ↓
PERMISSION REQUESTS
├── Allow Location (All the time)
├── Allow Camera
├── Allow Notifications
└── Allow Phone
          ↓
HOME SCREEN ✅
```

**Status**: ✅ Ready when home screen loads

---

## 🧪 Test Features

```
HOME SCREEN
├── 👨‍👩‍👧 Family Section
│   └── Shows empty (no family members yet)
│
├── 🔘 Quick Actions
│   ├── SOS Button (red)
│   ├── Contacts Button
│   └── Track Button
│
└── 📍 Locations
    └── Shows your emulator location

SOS SCREEN
├── Select reason: "Medical Emergency"
├── Click "TRIGGER SOS"
├── 30-second countdown starts
├── Click "Cancel" to abort
└── Or wait for auto-send

CONTACTS SCREEN
├── Click "+" to add contact
├── Enter:
│   ├── Name: Mom
│   ├── Phone: +1234567890
│   └── Relation: Mother
├── Click "Primary Contact"
└── Click "Save"
```

---

## 📊 Build Output

```
After successful build:

aegis/
├── app/
│   └── build/
│       └── outputs/
│           └── apk/
│               └── debug/
│                   └── app-debug.apk  ← Your APK here (20-30 MB)
│
└── (Android Studio shows message)
    "Build completed successfully"
```

**APK Location**: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔄 Common Issues & Fixes

```
❌ Gradle Sync Fails
↓
✅ Fix:
   $ ./gradlew clean
   $ ./gradlew build --refresh-dependencies

❌ google-services.json not found
↓
✅ Fix:
   1. Download from Firebase Console
   2. Place in app/google-services.json
   3. Rebuild

❌ API Key Error
↓
✅ Fix:
   1. Check gradle.properties has MAPS_API_KEY=...
   2. Verify key in Google Cloud Console
   3. Rebuild

❌ Emulator won't launch
↓
✅ Fix:
   $ pkill -9 qemu-system
   $ emulator -avd Pixel_4a_API_34

❌ App crashes on launch
↓
✅ Fix:
   1. Check: adb logcat -s Aegis
   2. Verify Firebase config
   3. Check internet on emulator
```

---

## ⚡ Fast Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on emulator
adb install app/build/outputs/apk/debug/app-debug.apk

# View logs
adb logcat -s Aegis

# List emulators
adb devices

# Uninstall app
adb uninstall com.karthik.aegis

# Clean build
./gradlew clean assembleDebug

# Full rebuild
./gradlew clean build
```

---

## 📁 Project Structure

```
aegis/
├── 📄 build.gradle.kts          ← Root build file
├── 📄 settings.gradle.kts
├── 📄 gradle.properties         ← Add MAPS_API_KEY here
│
├── app/
│   ├── 📄 build.gradle.kts      ← App dependencies
│   ├── 📄 google-services.json  ← Firebase config ⭐
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/karthik/aegis/
│   │   │   │   ├── AegisApplication.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── AuthScreen.kt
│   │   │   │   │   └── SOSScreen.kt
│   │   │   │   ├── service/
│   │   │   │   ├── repository/
│   │   │   │   ├── viewmodel/
│   │   │   │   └── utils/
│   │   │   │
│   │   │   └── res/
│   │   │       └── values/
│   │   │           └── strings.xml
│   │   │
│   │   └── build/
│   │       └── outputs/
│   │           └── apk/
│   │               └── debug/
│   │                   └── app-debug.apk  ← APK here
│   │
│   └── proguard-rules.pro
│
├── 📄 Dockerfile
├── 📄 docker-compose.yml
├── 📄 SETUP.md                  ← Setup guide
├── 📄 QUICK_START.md            ← This file
├── 📄 COMPLETE_GUIDE.md
└── 📄 README.md
```

---

## 🎯 Success Checklist

- [ ] Android Studio installed
- [ ] Firebase project created  
- [ ] google-services.json downloaded
- [ ] Maps API key configured
- [ ] Project cloned
- [ ] Gradle sync completed
- [ ] APK built successfully
- [ ] Emulator running
- [ ] App installed
- [ ] Auth screen shows
- [ ] Able to sign up
- [ ] Home screen loads
- [ ] Permissions working

---

## 🎓 What Happens Next

1. **Splash Screen** (2 seconds) - Loading animation
2. **Auth Screen** - Sign in/up options
3. **Permission Requests** - Location, camera, notifications
4. **Home Screen** - Main app interface with:
   - Family members list
   - Live locations
   - Active alerts
   - Quick action buttons (SOS, Contacts, Track)

---

## 🚀 Advanced Options

### Run with Docker

```bash
# Build Docker image
docker-compose build

# Run build in container
docker-compose up

# Extract APK from container
mkdir -p build-output
docker-compose cp aegis-builder:/app/app/build/outputs/apk/debug/. build-output/
```

### Run on Physical Device

```bash
# Enable Developer Mode on phone
Settings → About phone → Tap "Build number" 7 times

# Connect via USB
adb devices  # Should show your phone

# Install & run
./gradlew installDebug
```

### Debug Mode

```bash
# Enable debug logging
adb logcat -s Aegis

# View Firebase logs
adb logcat -s FirebaseAuth
adb logcat -s Firestore

# View location logs
adb logcat -s LocationTrackingService
```

---

## 📞 Need Help?

1. **Check Logs**: `adb logcat -s Aegis`
2. **Read Docs**: SETUP.md, COMPLETE_GUIDE.md
3. **Firebase Console**: Verify project settings
4. **GitHub**: Report issues

---

<div align="center">

## ✅ READY TO RUN!

Follow the 15-minute setup above and you'll have Aegis running.

**Your Family. Protected. Always. 🛡️**

</div>
