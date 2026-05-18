# 🎬 HOW TO RUN AEGIS - THE COMPLETE ANSWER

This document answers: **"How do I actually run this project?"**

---

## ⚡ THE FASTEST WAY (Copy & Paste)

If you have everything already downloaded:

```bash
# 1. Go to project folder
cd aegis

# 2. Build APK
./gradlew assembleDebug

# 3. Install on emulator
adb install app/build/outputs/apk/debug/app-debug.apk

# 4. Launch app
adb shell am start -n com.karthik.aegis/.ui.MainActivity

# 5. View logs (optional)
adb logcat -s Aegis
```

**Done!** App is running.

---

## 📋 PREREQUISITES (Must Have)

Before you can run, ensure you have:

### 1. Android Studio ✅
- Download: https://developer.android.com/studio
- Install it
- JDK 17+ (included in Android Studio)

### 2. Firebase Project ✅
- Create at: https://console.firebase.google.com
- Download `google-services.json`
- Place in: `app/google-services.json`

### 3. Maps API Key ✅
- Get from: https://console.cloud.google.com
- Add to: `gradle.properties` as `MAPS_API_KEY=...`

### 4. Project Files ✅
- Clone: `git clone https://github.com/MKarthik730/aegis.git`
- Or download zip and extract

---

## 🎯 STEP-BY-STEP (What to Do)

### Phase 1: Preparation (5 minutes)

```
1. Create Firebase project
   └── Download google-services.json
   └── Enable: Auth, RTDB, Firestore

2. Get Maps API key
   └── Enable Maps SDK for Android
   └── Create API Key

3. Get project files
   └── Clone or download project

4. Place files correctly:
   └── app/google-services.json
   └── gradle.properties (add MAPS_API_KEY=...)
```

### Phase 2: Build (5 minutes)

```
1. Open Android Studio

2. File → Open → Select aegis folder

3. Wait for Gradle sync
   └── Should complete without errors

4. Build → Build Bundle/APK → Build APK

5. Wait for build to complete
   └── See "Build completed successfully"
```

### Phase 3: Run (5 minutes)

```
1. Device Manager → Create device
   └── Pixel 4a, API 34

2. Click Play button to launch emulator

3. Android Studio → Run (green play button)

4. Select emulator

5. App installs and launches

6. Sign up with test account
   └── test@aegis.app / testpassword123

7. Grant permissions when asked

8. Home screen appears ✅
```

---

## 📱 RUN ON EMULATOR

### Create Emulator

```
Android Studio → Device Manager
├── Click "Create device"
├── Select: Pixel 4a
├── Select: Android 14 (API 34)
├── Click "Finish"
└── Click Play (▶️) button
```

### Launch App on Emulator

```
Method 1: Android Studio UI
├── Top toolbar → Run (green ▶️ button)
├── Select emulator
└── App installs & launches

Method 2: Command Line
├── adb devices (verify emulator appears)
├── adb install app/build/outputs/apk/debug/app-debug.apk
└── adb shell am start -n com.karthik.aegis/.ui.MainActivity
```

---

## 📱 RUN ON PHYSICAL DEVICE

### Setup Phone

```
Settings → About phone
├── Tap "Build number" 7 times
└── Developer options appears

Settings → Developer options
├── Enable "USB Debugging"
└── Connect phone via USB cable
```

### Install App

```bash
# Verify connection
adb devices

# Install
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.karthik.aegis/.ui.MainActivity
```

---

## 🐳 RUN WITH DOCKER

For containerized build:

```bash
# Build Docker image
docker-compose build

# Run build in container
docker-compose up

# Extract APK
mkdir build-output
docker-compose exec aegis-builder sh -c "cp /app/app/build/outputs/apk/debug/*.apk /app/output/"

# APK is in ./build-output/
```

---

## ✅ VERIFICATION STEPS

After launching, verify you see:

```
✅ Splash screen (Aegis logo)
   ↓ (2 seconds)
✅ Auth screen (email/password fields)
   ↓ (click "Sign Up")
✅ Sign up form (name, email, password)
   ↓ (enter test@aegis.app / testpassword123)
✅ Permission requests
   ✓ Allow location
   ✓ Allow camera
   ✓ Allow notifications
   ↓
✅ Home screen
   ├─ Family section (empty)
   ├─ SOS button (red)
   ├─ Contacts button
   └─ Track button
```

---

## 🧪 TEST FEATURES

### Test SOS Feature
```
1. Click "SOS" button (red)
2. Select reason: "Medical Emergency"
3. Click "TRIGGER SOS"
4. 30-second countdown appears
5. Click "Cancel" to stop
```

### Add Emergency Contact
```
1. Click "Contacts" button
2. Click "+" button
3. Enter:
   ├─ Name: Mom
   ├─ Phone: +1-555-0123
   └─ Relation: Mother
4. Check "Primary Contact"
5. Click "Save"
```

### Set Test Location
```
1. In emulator: Extended controls
2. Location tab
3. Enter coordinates: 37.7749, -122.4194
4. Click "Set Location"
5. App shows location on home screen
```

---

## ⚠️ IF SOMETHING GOES WRONG

### Gradle Sync Fails
```bash
cd aegis
./gradlew clean
./gradlew build --refresh-dependencies
```

### App Won't Build
```bash
# Check for errors
./gradlew build -i

# Or clean and try again
./gradlew clean assembleDebug
```

### App Crashes on Launch
```bash
# Check logs
adb logcat -s Aegis

# Look for errors and fix Firebase config
# Then rebuild and reinstall
```

### Emulator Won't Start
```bash
# Kill all emulator processes
pkill -9 qemu-system

# Or recreate emulator
android delete avd -n Pixel_4a_API_34
```

### App Shows "Firebase Error"
```
1. Verify google-services.json is in app/
2. Check Firebase Console has services enabled
3. Verify internet connection on emulator
4. Rebuild project
```

---

## 📊 EXPECTED FILE STRUCTURE

After successful setup:

```
aegis/
├── gradle.properties        ← MAPS_API_KEY=...
├── build.gradle.kts
├── settings.gradle.kts
│
├── app/
│   ├── google-services.json ← From Firebase ⭐
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/karthik/aegis/
│   │   └── res/
│   │
│   └── build/               ← Created after build
│       └── outputs/
│           └── apk/
│               └── debug/
│                   └── app-debug.apk ✅
│
├── Dockerfile
├── docker-compose.yml
├── HOW_TO_RUN.md
├── QUICK_START.md
├── SETUP.md
└── README.md
```

---

## 🎯 SUCCESS CHECKLIST

Complete this to know you're done:

- [ ] Android Studio opened successfully
- [ ] Project opened in Android Studio
- [ ] Gradle synced (no errors)
- [ ] APK built successfully
- [ ] Emulator created and running
- [ ] App installed on emulator
- [ ] Splash screen appeared
- [ ] Auth screen visible
- [ ] Able to sign up
- [ ] Permission requests appeared
- [ ] Home screen loaded
- [ ] No crashes or errors

---

## 🚀 WHAT'S NEXT

After app is running:

1. **Explore the app** - Test all features
2. **Customize it** - Change colors, texts, branding
3. **Add features** - Modify code as needed
4. **Deploy** - Build release APK for Play Store
5. **Share** - Distribute to family

---

## 📚 DOCUMENTATION

For more detailed info:

- **HOW_TO_RUN.md** - This detailed guide
- **QUICK_START.md** - Quick reference
- **SETUP.md** - Complete setup guide
- **COMPLETE_GUIDE.md** - Architecture & features
- **QUICK_REFERENCE.md** - Copy-paste commands

---

## 🔗 RESOURCES

- Android Studio: https://developer.android.com/studio
- Firebase: https://console.firebase.google.com
- Google Cloud: https://console.cloud.google.com
- Android Docs: https://developer.android.com/docs
- Kotlin Docs: https://kotlinlang.org/docs/

---

## 💡 COMMON MISTAKES TO AVOID

❌ Forgetting google-services.json
❌ Wrong package name (must be: com.karthik.aegis)
❌ Missing MAPS_API_KEY in gradle.properties
❌ Emulator API too old (use API 30+)
❌ Not granting permissions
❌ Firebase services not enabled
❌ Gradle cache issues

---

## 📞 NEED HELP?

1. **Check logcat**: `adb logcat -s Aegis`
2. **Read docs**: Check files listed above
3. **Firebase**: Verify Console settings
4. **GitHub**: Report issues

---

<div align="center">

## ✅ YOU CAN DO THIS!

Just follow these steps and Aegis will be running in 15 minutes.

**Print or bookmark this page for reference.**

---

### The Complete Answer to "How to Run Aegis"

1. ✅ Install Android Studio
2. ✅ Create Firebase project
3. ✅ Get Maps API key
4. ✅ Download google-services.json
5. ✅ Clone project
6. ✅ Build APK
7. ✅ Create emulator
8. ✅ Install & run
9. ✅ Sign up
10. ✅ Grant permissions

**Done! App is running! 🎉**

---

**Your Family. Protected. Always. 🛡️**

</div>
