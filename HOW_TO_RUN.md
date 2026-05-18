# 🎯 HOW TO RUN AEGIS - COMPLETE INSTRUCTIONS

## 15-Minute Quick Start

### Prerequisites Checklist
- ✅ Computer with 4GB+ RAM
- ✅ Internet connection
- ✅ 5GB+ free disk space

---

## 🚀 THE FASTEST WAY (Command Line)

If you're comfortable with terminal/command line:

```bash
# 1. Clone project (if not already done)
git clone https://github.com/MKarthik730/aegis.git
cd aegis

# 2. Download google-services.json from Firebase Console
# Place it in: aegis/app/google-services.json

# 3. Add Maps API key to gradle.properties
echo "MAPS_API_KEY=your_api_key_here" >> gradle.properties

# 4. Build the APK
./gradlew assembleDebug

# 5. Install on emulator or connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# 6. Launch app
adb shell am start -n com.karthik.aegis/.ui.MainActivity
```

**Done!** App is now running.

---

## 🎨 STEP-BY-STEP GUIDE (Recommended for First-Timers)

### Step 1️⃣: Download & Install Android Studio

**Time: 10-15 minutes**

1. Go to: https://developer.android.com/studio
2. Download for your OS (Windows, Mac, Linux)
3. Run installer and follow wizard
4. When asked, install:
   - Android SDK
   - Android Emulator
   - SDK Tools
5. Complete installation (may take 5-10 minutes)

✅ **Verify**: Open Android Studio and it should launch successfully

---

### Step 2️⃣: Create Firebase Project

**Time: 5 minutes**

1. Go to: https://console.firebase.google.com
2. Click **"Create a project"** (blue button)
3. Enter name: **"aegis-demo"**
4. Accept terms → **"Create project"** → Wait 1-2 min

**Now add Android app:**

5. On welcome page, click the **Android icon** (</> button)
6. Enter package name: **`com.karthik.aegis`**
7. Click **"Register app"**
8. Click **"Download google-services.json"**
9. Keep this file safe (you'll need it in Step 5)

**Now enable required services:**

10. Go to **"Authentication"** tab
    - Click **"Get Started"**
    - Enable **"Email/Password"**
    - Enable **"Phone"** (optional)

11. Go to **"Realtime Database"** tab
    - Click **"Create Database"**
    - Select your region
    - Choose **"Start in test mode"** (for development)

12. Go to **"Cloud Firestore"** tab
    - Click **"Create database"**
    - Select same region
    - Choose **"Start in test mode"**

✅ **Verify**: All three services show "Enabled" in Firebase Console

---

### Step 3️⃣: Get Maps API Key

**Time: 3 minutes**

1. Go to: https://console.cloud.google.com
2. Make sure your Firebase project is selected (top dropdown)
3. Search for: **"Maps SDK for Android"**
4. Click **"Enable"**
5. Go to **"Credentials"** in left sidebar
6. Click **"Create Credentials"** → **"API Key"**
7. Copy the key that appears

✅ **Verify**: You have a long string like `AIzaSyD...`

---

### Step 4️⃣: Set Up Project Files

**Time: 2 minutes**

1. Download the Aegis project (or clone with git)
2. Extract to a folder: `/path/to/aegis`
3. You should see these files:
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `app/` folder
   - `Dockerfile`
   - `SETUP.md`

✅ **Verify**: Folder has all expected files

---

### Step 5️⃣: Add Firebase Configuration

**Time: 2 minutes**

1. Copy the **`google-services.json`** file from Step 2
2. Place it in: `aegis/app/google-services.json`

```
aegis/
├── app/
│   ├── google-services.json  ← Place here
│   ├── build.gradle.kts
│   └── src/
```

3. Open `gradle.properties` file (in aegis root)
4. Add this line:
   ```properties
   MAPS_API_KEY=AIzaSyD...
   ```
   (Replace with your API key from Step 3)

✅ **Verify**: Both files are in correct locations

---

### Step 6️⃣: Open Project in Android Studio

**Time: 5 minutes**

1. Open Android Studio
2. Click **"File"** → **"Open"**
3. Navigate to `aegis` folder
4. Click **"Open"**
5. Wait for Gradle sync to complete (3-5 minutes)
6. You should see: **"Gradle build finished"** at bottom

✅ **Verify**: No red errors in Gradle console

**If Gradle sync fails:**
```bash
cd aegis
./gradlew clean
./gradlew build --refresh-dependencies
```

---

### Step 7️⃣: Build APK

**Time: 5 minutes**

**Method A: Using Android Studio UI (Recommended)**

1. In top menu: **"Build"** → **"Build Bundle(s)/APK(s)"** → **"Build APK(s)"**
2. Wait for build (should show progress bar)
3. When complete, you'll see: **"Build completed successfully"**
4. Click **"locate"** to find your APK

**Method B: Using Terminal**

```bash
cd aegis
./gradlew assembleDebug
```

Wait for message: **"BUILD SUCCESSFUL"**

✅ **Verify**: APK is built at `app/build/outputs/apk/debug/app-debug.apk` (~25 MB)

---

### Step 8️⃣: Create & Launch Emulator

**Time: 5 minutes**

1. In Android Studio, click **"Device Manager"** (right sidebar, usually at bottom)
2. Click **"Create device"**
3. Select: **"Pixel 4a"** (or any phone model)
4. Click **"Next"**
5. Select: **"Android 14 (API 34)"** (or latest available)
6. Click **"Next"**
7. Review settings and click **"Finish"**
8. You'll see device created. Click the **▶️ Play button** to launch

**Wait 1-2 minutes for emulator to boot**

✅ **Verify**: You see Android home screen

---

### Step 9️⃣: Install & Run App

**Time: 3 minutes**

1. In Android Studio, click **▶️ Run** button (top toolbar, green play icon)
2. Select the emulator you just created
3. Click **"OK"**
4. Wait 1-2 minutes for app to install and launch

**You should see:**
1. **Splash screen** with Aegis logo (2 seconds)
2. **Auth screen** with email/password fields

✅ **Verify**: Auth screen is visible

---

### Step 🔟: First Launch & Setup

**Time: 3 minutes**

1. On **Auth screen**, click **"Sign Up"**
2. Enter:
   ```
   Full Name: Test User
   Email: test@aegis.app
   Password: testpassword123
   ```
3. Click **"Create Account"**
4. App will request permissions:
   - ✅ Allow **Location** (choose "Allow all the time")
   - ✅ Allow **Camera**
   - ✅ Allow **Notifications**
   - ✅ Allow **Phone**
5. Grant all permissions

**You should see:**
- **Home screen** with Aegis features
- Empty family members section
- Quick action buttons (SOS, Contacts, Track)

✅ **Verify**: App is running and responsive

---

## 🧪 Test Features

### Test SOS
1. Click **"SOS"** button (red)
2. Select reason: "Medical Emergency"
3. Click **"TRIGGER SOS"**
4. **30-second countdown** appears
5. Click **"Cancel"** to stop

### Add Emergency Contact
1. Click **"Contacts"** button
2. Click **"+"** to add
3. Enter:
   ```
   Name: Mom
   Phone: +1-555-0123
   Relation: Mother
   ```
4. Check **"Primary Contact"**
5. Click **"Save"**

### Set Location (Emulator)
1. In Android Studio, find **"Extended controls"** button (emulator)
2. Go to **"Location"** tab
3. Set coordinates: `37.7749, -122.4194`
4. Click **"Set Location"**
5. Return to app and watch location update

---

## 🐳 Alternative: Run with Docker

If you prefer containerization:

```bash
cd aegis

# Build Docker image
docker-compose build

# Run the build
docker-compose up

# In another terminal, extract APK
mkdir -p build-output
docker-compose exec aegis-builder sh -c "cp /app/app/build/outputs/apk/debug/*.apk /app/output/" || true
```

APK will be in `./build-output/`

---

## 📱 Run on Physical Android Phone

### Enable Developer Mode
1. Go to **Settings** → **About phone**
2. Tap **"Build number"** 7 times
3. Go back to Settings
4. Find **"Developer options"** (newly appeared)
5. Enable **"USB Debugging"**

### Connect & Run
```bash
# Connect phone via USB cable

# Verify connection
adb devices

# Install app
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.karthik.aegis/.ui.MainActivity
```

---

## ⚠️ Common Problems & Solutions

### Problem: "Gradle sync failed"
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Problem: "google-services.json not found"
- Download from Firebase Console
- Place in: `app/google-services.json`
- Rebuild

### Problem: "API Key error"
- Check `gradle.properties` has `MAPS_API_KEY=...`
- Verify key is enabled in Google Cloud Console
- Rebuild project

### Problem: "Emulator won't start"
```bash
pkill -9 qemu-system
emulator -avd Pixel_4a_API_34
```

### Problem: "App crashes immediately"
```bash
# Check logs
adb logcat -s Aegis

# Common fixes:
# 1. Verify google-services.json is correct
# 2. Check internet connection on emulator
# 3. Ensure Firebase services are enabled
```

### Problem: "Emulator has no GPS location"
- Click **Extended controls** in emulator
- Go to **Location** tab
- Enter test coordinates
- Click **Set Location**

---

## 🎯 Verify Success

When app is running, you should see:

✅ Splash screen with logo
✅ Auth screen (email/password)
✅ Able to sign up
✅ Permission requests
✅ Home screen loads
✅ Family section visible
✅ Buttons responsive
✅ No crashes or errors

---

## 📊 File Locations

```
aegis/
├── google-services.json      ← Downloaded from Firebase ⭐
├── gradle.properties         ← Contains MAPS_API_KEY ⭐
├── app/
│   ├── google-services.json  ← Copy here ⭐
│   └── build/
│       └── outputs/
│           └── apk/
│               └── debug/
│                   └── app-debug.apk  ← Your APK
├── SETUP.md
├── QUICK_START.md
├── RUN_GUIDE.md
└── COMPLETE_GUIDE.md
```

---

## 📚 What Each File Does

| File | Purpose |
|------|---------|
| `google-services.json` | Firebase configuration (JSON) |
| `gradle.properties` | Build properties (includes API key) |
| `build.gradle.kts` | Project dependencies |
| `AndroidManifest.xml` | App permissions & components |
| `MainActivity.kt` | App entry point |

---

## 🔗 Useful Links

- **Android Studio**: https://developer.android.com/studio
- **Firebase Console**: https://console.firebase.google.com
- **Google Cloud Console**: https://console.cloud.google.com
- **Android Docs**: https://developer.android.com/docs
- **Kotlin Docs**: https://kotlinlang.org/docs/

---

## 📞 Need More Help?

1. **Check logs**: `adb logcat -s Aegis`
2. **Read docs**: See SETUP.md or COMPLETE_GUIDE.md
3. **Firebase issues**: Check Firebase Console settings
4. **Report bugs**: GitHub issues

---

<div align="center">

## ✅ YOU'RE READY!

Follow the 10 steps above and Aegis will be running in 15 minutes.

If you get stuck, check the **"Common Problems"** section.

**Your Family. Protected. Always. 🛡️**

</div>
