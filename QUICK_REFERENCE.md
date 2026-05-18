# ⚡ AEGIS - QUICK REFERENCE CARD

Print this or save on your phone for quick reference while setting up.

---

## 🎯 5-STEP QUICK START

```
STEP 1: Android Studio
   Download: https://developer.android.com/studio
   Install: Follow wizard
   Time: 10 min
   ✅ Done when: Studio opens

STEP 2: Firebase Project  
   Go to: https://console.firebase.google.com
   Create: New project
   Add: Android app (com.karthik.aegis)
   Download: google-services.json
   Enable: Auth, RTDB, Firestore
   Time: 5 min
   ✅ Done when: Services enabled

STEP 3: Maps API Key
   Go to: https://console.cloud.google.com
   Search: Maps SDK for Android
   Enable: API
   Create: API Key
   Copy: Key string
   Time: 3 min
   ✅ Done when: Key copied

STEP 4: Setup Project
   Place: google-services.json in app/
   Add: MAPS_API_KEY=... to gradle.properties
   Open: Project in Android Studio
   Sync: Gradle (wait for "BUILD SUCCESSFUL")
   Time: 5 min
   ✅ Done when: No Gradle errors

STEP 5: Build & Run
   Build: ./gradlew assembleDebug
   Create: Emulator (Pixel 4a, API 34)
   Install: adb install app/build/outputs/apk/debug/app-debug.apk
   Launch: App from emulator
   Sign up: test@aegis.app / testpassword123
   Time: 5 min
   ✅ Done when: App shows home screen
```

---

## 🚀 ESSENTIAL COMMANDS

```bash
# Build
./gradlew clean assembleDebug
./gradlew assembleRelease

# Check
adb devices
adb logcat -s Aegis

# Install
adb install app/build/outputs/apk/debug/app-debug.apk

# Run
adb shell am start -n com.karthik.aegis/.ui.MainActivity

# Uninstall
adb uninstall com.karthik.aegis

# Clean
./gradlew clean
```

---

## 📁 CRITICAL FILES

```
MUST PLACE THESE:
├── app/google-services.json     ← From Firebase Console
├── gradle.properties            ← Add MAPS_API_KEY=...
└── app/build.gradle.kts         ← Already has dependencies
```

---

## 🔑 LOGIN CREDENTIALS

```
Test Account:
├── Email: test@aegis.app
├── Password: testpassword123
└── Name: Test User

Or use demo:
├── Email: demo@aegis.app
├── Password: demo123456
└── Name: Demo User
```

---

## ✅ VERIFICATION CHECKLIST

```
Before Building:
[ ] Android Studio installed
[ ] Firebase project created
[ ] google-services.json downloaded
[ ] MAPS_API_KEY obtained
[ ] Files in correct locations
[ ] Gradle synced (no errors)

After Building:
[ ] APK built successfully
[ ] Emulator created (Pixel 4a, API 34)
[ ] App installs on emulator
[ ] Splash screen appears
[ ] Auth screen shows
[ ] Sign up works
[ ] Home screen loads

After Launch:
[ ] Family section visible
[ ] SOS button works (30s countdown)
[ ] Contacts button works
[ ] Can add emergency contact
[ ] Permissions working
[ ] No crashes or errors
```

---

## 🐛 QUICK FIXES

```
Gradle Fails:
  ./gradlew clean
  ./gradlew build --refresh-dependencies

Emulator Won't Start:
  pkill -9 qemu-system
  emulator -avd Pixel_4a_API_34

App Crashes:
  adb logcat -s Aegis
  Check: Firebase config
  Check: google-services.json location
  Check: Permissions granted

Firebase Error:
  1. Check google-services.json is correct
  2. Check Firebase Console has services enabled
  3. Check package name: com.karthik.aegis

API Key Error:
  1. gradle.properties has MAPS_API_KEY=...
  2. Key is valid in Google Cloud Console
  3. Maps SDK is enabled
```

---

## 📱 EMULATOR SETUP

```
Device: Pixel 4a
API Level: 34 (Android 14)
RAM: 4GB
Storage: 2GB
GPU: Enabled
```

---

## 🌐 EXTERNAL LINKS

```
Android Studio: https://developer.android.com/studio
Firebase: https://console.firebase.google.com
Google Cloud: https://console.cloud.google.com
Kotlin Docs: https://kotlinlang.org/docs/
Android Docs: https://developer.android.com/docs
```

---

## ⏱️ TIME ESTIMATES

```
Android Studio Install:     10-15 min
Firebase Setup:             5 min
Maps API Key:               3 min
Project Setup:              5 min
Build APK:                  5 min
Emulator Create:            3 min
Install & Run:              3 min
─────────────────────────────────
TOTAL:                      35-40 min

Note: Most steps can run in parallel
Actual waiting time: ~15-20 min
```

---

## 📊 FILE SIZES

```
Android Studio:            ~1.2 GB
Android SDK:               ~10 GB
Project Files:             ~100 MB
APK (Debug):               ~25 MB
APK (Release):             ~15 MB

Total needed:              ~15 GB free disk space
```

---

## 🎯 SUCCESS INDICATORS

App is running correctly when:
✅ Splash screen appears (2 seconds)
✅ Auth screen loads
✅ Sign up/sign in works
✅ Permissions dialog appears
✅ Home screen shows
✅ Family section visible
✅ Buttons are clickable
✅ No red errors in logcat

---

## 🆘 SUPPORT RESOURCES

```
Documentation:
├── HOW_TO_RUN.md         ← Step-by-step (this info)
├── QUICK_START.md        ← Quick reference
├── SETUP.md              ← Detailed setup
└── COMPLETE_GUIDE.md     ← Full guide

Logs:
├── adb logcat -s Aegis
├── adb logcat -s FirebaseAuth
└── adb logcat -s LocationTrackingService

Firebase Console:
├── Authentication tab
├── Realtime Database tab
├── Cloud Firestore tab
└── Cloud Messaging tab
```

---

## 🚨 COMMON MISTAKES

❌ Wrong package name: (should be: com.karthik.aegis)
❌ Missing google-services.json (check app/ folder)
❌ Missing MAPS_API_KEY in gradle.properties
❌ Firebase services not enabled (check console)
❌ Emulator API too old (use API 30+)
❌ Not granting permissions on first launch
❌ Gradle cache issues (./gradlew clean)

---

## 💡 PRO TIPS

✅ Use Android Studio's Device Manager for emulator
✅ Enable GPU in emulator settings for speed
✅ Use Pixel 4a (good balance of performance)
✅ Keep Android SDK updated
✅ Use Test Mode in Firebase during development
✅ Check logcat often during debugging
✅ Clear app data if behaving weird: adb shell pm clear com.karthik.aegis
✅ Use "adb devices" to verify connection

---

## 🎓 LEARNING PATH

1. **First Time**: Follow HOW_TO_RUN.md step by step
2. **Understand**: Read SETUP.md after first launch
3. **Deep Dive**: Study COMPLETE_GUIDE.md for architecture
4. **Customize**: Modify code based on your needs
5. **Deploy**: Use build.gradle.kts to release

---

## 📞 QUICK CONTACT

For issues:
1. Check the troubleshooting section above
2. Check logcat: adb logcat -s Aegis
3. Read documentation in project root
4. Check Firebase Console settings
5. Report on GitHub issues

---

<div align="center">

## ⚡ BOOKMARK THIS PAGE

Save this for quick reference while setting up.

**Time Estimate: 15-20 minutes to running app**

**Your Family. Protected. Always. 🛡️**

</div>

---

## 📋 COPY-PASTE COMMANDS

```bash
# Full setup in one go (after files are in place)
./gradlew clean
./gradlew assembleDebug
adb devices
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.karthik.aegis/.ui.MainActivity
adb logcat -s Aegis
```

---

## 🎯 NEXT ACTIONS

After app is running:

1. **Test SOS feature**
   - Click SOS button
   - Watch 30-second countdown
   - Click Cancel to stop

2. **Add emergency contact**
   - Go to Contacts
   - Click +
   - Enter contact info
   - Mark as Primary

3. **Set test location**
   - Open emulator Extended Controls
   - Go to Location tab
   - Enter coordinates
   - Check location on app

4. **Test notifications**
   - Enable notifications
   - Verify Firebase FCM setup

5. **Explore home screen**
   - View family members
   - See live tracking
   - Check active alerts

---

That's it! You're ready to run Aegis! 🚀
