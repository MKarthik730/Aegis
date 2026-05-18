# 🚀 AEGIS - QUICK START GUIDE

## How to Run the Project

This guide will help you get Aegis running in 15-20 minutes.

---

## 📋 Prerequisites

Before you start, ensure you have:

1. **Android Studio** (Hedgehog 2023.1.1 or later)
   - Download: https://developer.android.com/studio

2. **JDK 17 or higher**
   - Included with Android Studio
   - Or: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html

3. **Firebase Account**
   - Free tier available at: https://console.firebase.google.com

4. **Google Cloud Project** (for Maps API)
   - https://console.cloud.google.com

---

## ✅ Step 1: Install Android Studio (5 minutes)

### Mac/Linux/Windows:
1. Download Android Studio from: https://developer.android.com/studio
2. Run the installer
3. Follow setup wizard
4. When prompted, install:
   - Android SDK
   - Android Emulator
   - Android SDK Tools

### Verify Installation:
```bash
# Check if Android SDK is installed
ls ~/Library/Android/sdk  # macOS/Linux
# or
dir %LOCALAPPDATA%\Android\sdk  # Windows
```

---

## 🔥 Step 2: Setup Firebase Project (5 minutes)

### Create Firebase Project:

1. Go to https://console.firebase.google.com
2. Click **"Create a project"**
3. Enter project name: **"aegis-demo"**
4. Accept terms and click **"Create project"**
5. Wait for project creation (1-2 minutes)

### Add Android App:

1. Click the **Android icon** (</>) on the welcome page
2. Enter package name: **`com.karthik.aegis`**
3. App nickname: **"Aegis" (optional)**
4. Click **"Register app"**
5. Download **`google-services.json`**
6. Click **"Next"** through remaining steps

### Enable Services:

In Firebase Console, go to:

1. **Authentication**
   - Click **"Get started"**
   - Enable: **Email/Password**
   - Enable: **Phone**

2. **Realtime Database**
   - Click **"Create Database"**
   - Select region: closest to you
   - Start in **"Test mode"** (for development)

3. **Cloud Firestore**
   - Click **"Create database"**
   - Select region: same as RTDB
   - Start in **"Test mode"**

4. **Cloud Messaging**
   - Go to **"Cloud Messaging"** tab
   - Note your **Server API Key** (for later)

---

## 🗺️ Step 3: Configure Maps API (3 minutes)

### Get Google Maps API Key:

1. Go to: https://console.cloud.google.com
2. Select your Firebase project
3. Search for **"Maps SDK for Android"**
4. Click **"Enable"**
5. Go to **"Credentials"**
6. Click **"Create Credentials"**
7. Select **"API Key"**
8. Copy the API key

### Add to Project:

Create/edit `gradle.properties` in project root:

```properties
MAPS_API_KEY=YOUR_API_KEY_HERE
```

Replace `YOUR_API_KEY_HERE` with the key from step above.

---

## 📂 Step 4: Clone Project (2 minutes)

### Option A: Using Git

```bash
git clone https://github.com/MKarthik730/aegis.git
cd aegis
```

### Option B: Using Downloaded Files

If you already have the files:
```bash
cd /path/to/aegis
```

---

## 🔑 Step 5: Add Firebase Configuration (2 minutes)

1. Copy `google-services.json` from Firebase Console
2. Place it in: `app/google-services.json`

```
aegis/
├── app/
│   ├── google-services.json  ← Place here
│   ├── src/
│   └── build.gradle.kts
└── ...
```

---

## 🏗️ Step 6: Open in Android Studio (3 minutes)

1. Open Android Studio
2. Click **"Open"**
3. Navigate to aegis project folder
4. Click **"Open"**
5. Wait for Gradle sync (2-3 minutes)

### If Gradle Sync Fails:

```bash
cd aegis
./gradlew clean
./gradlew build
```

---

## 🔨 Step 7: Build Project (5 minutes)

### In Android Studio:

1. Go to **"Build"** → **"Build Bundle(s)/APK(s)"** → **"Build APK(s)"**
2. Wait for build to complete (usually 3-5 minutes)
3. You'll see message: **"Build completed successfully"**

### From Command Line:

```bash
cd aegis
./gradlew assembleDebug
```

---

## ▶️ Step 8: Run on Emulator (5 minutes)

### Launch Emulator:

1. In Android Studio, click **"Device Manager"** (bottom right)
2. Click **"Create device"**
3. Select device: **"Pixel 4a"** (or any device)
4. Select Android version: **API 34** (or latest)
5. Click **"Finish"**
6. Click the **▶️ Play button** to launch emulator

### Install & Run App:

1. Android Studio will auto-detect emulator
2. Click **▶️ Run** (green play button, top toolbar)
3. Select emulator when prompted
4. Wait for app to install & launch (1-2 minutes)

### From Command Line:

```bash
# List connected emulators
adb devices

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.karthik.aegis/.ui.MainActivity
```

---

## 🧪 Step 9: Test the App (5 minutes)

### First Launch:

1. **Splash Screen** (2 seconds)
2. **Auth Screen** appears
3. Choose: **"Sign Up"**

### Create Test Account:

```
Full Name: Test User
Email: test@aegis.app
Password: testpassword123
```

Or use demo credentials:

```
Email: demo@aegis.app
Password: demo123456
```

### Grant Permissions:

When prompted, allow:
- ✅ Location (All the time)
- ✅ Camera (Allow)
- ✅ Notifications (Allow)
- ✅ Phone (Allow)

### Test Features:

1. **Home Screen**
   - Shows family members
   - Live locations
   - Active alerts

2. **SOS Screen**
   - Click "TRIGGER SOS"
   - Select reason: "Medical Emergency"
   - 30-second countdown appears
   - Click "Cancel" to abort

3. **Contacts Screen**
   - Click "+" to add emergency contact
   - Enter name, phone, relation
   - Mark as "Primary Contact"
   - Click "Save"

4. **Location Tracking**
   - Emulator doesn't have GPS
   - In Android Studio, go to **Extended controls** → **Location**
   - Set test location: 37.7749, -122.4194 (San Francisco)
   - Watch live location update

---

## 🐳 Alternative: Run with Docker (10 minutes)

### Prerequisites:

- Docker Desktop installed
- 5GB+ free disk space

### Build Docker Image:

```bash
cd aegis
docker-compose build
```

### Run Build in Docker:

```bash
docker-compose up
```

### Extract APK:

```bash
mkdir -p build-output
docker-compose exec aegis-builder sh -c "cp /app/app/build/outputs/apk/debug/*.apk /app/output/" || true
```

APK will be in `./build-output/`

---

## 📱 Run on Physical Device

### Enable Developer Mode:

1. Go to **Settings** → **About phone**
2. Tap **"Build number"** 7 times
3. Go back, find **"Developer options"**
4. Enable: **"USB Debugging"**

### Connect Device:

```bash
# List connected devices
adb devices

# Should show your device
```

### Run App:

```bash
./gradlew installDebug
```

Or in Android Studio: Click **▶️ Run** and select device.

---

## ✅ Verify Installation

After launching app, you should see:

1. ✅ Splash screen with Aegis logo
2. ✅ Auth screen with email/password fields
3. ✅ Ability to sign up/sign in
4. ✅ Permission requests (location, camera, etc.)
5. ✅ Home screen with family members section

---

## 🐛 Troubleshooting

### Problem: Gradle Sync Fails

**Solution:**
```bash
cd aegis
./gradlew clean
./gradlew build --refresh-dependencies
```

### Problem: "google-services.json not found"

**Solution:**
1. Download from Firebase Console
2. Place in `app/google-services.json`
3. Rebuild project

### Problem: API Key Error

**Solution:**
1. Check `gradle.properties` has `MAPS_API_KEY=...`
2. Verify API key is valid in Google Cloud Console
3. Ensure Maps SDK is enabled

### Problem: Emulator Won't Start

**Solution:**
```bash
# Kill all emulator processes
pkill -9 qemu-system

# Or delete and recreate emulator
cd aegis
emulator -list-avds
emulator -avd Pixel_4a_API_34
```

### Problem: "Permission denied" on Linux

**Solution:**
```bash
chmod +x gradlew
./gradlew assembleDebug
```

### Problem: App Crashes on Launch

**Solution:**
1. Check logcat: `adb logcat -s Aegis`
2. Ensure Firebase is configured
3. Verify `google-services.json` is correct
4. Check internet connection on emulator

---

## 📊 Check Build Logs

### View detailed logs:

```bash
# Build with verbose output
./gradlew assembleDebug -i

# View app logs
adb logcat -s Aegis
adb logcat -s FirebaseAuth
adb logcat -s LocationTrackingService
```

---

## 🔗 Useful Commands

### Clean & Rebuild:
```bash
./gradlew clean assembleDebug
```

### Run Tests:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### View APK:
```bash
# After build
ls app/build/outputs/apk/debug/

# Install directly
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Check Device Info:
```bash
adb devices -l
adb shell getprop ro.build.version.release
```

### Uninstall App:
```bash
adb uninstall com.karthik.aegis
```

---

## 🎯 Quick Start Checklist

- [ ] Android Studio installed
- [ ] Firebase project created
- [ ] google-services.json downloaded
- [ ] Maps API key obtained
- [ ] gradle.properties configured with MAPS_API_KEY
- [ ] Project cloned/setup
- [ ] Gradle sync completed
- [ ] APK built successfully
- [ ] Emulator launched
- [ ] App installed & running
- [ ] Able to sign in
- [ ] Permissions granted

---

## 📚 Next Steps

1. **Test Features**: Try SOS, contacts, location tracking
2. **Customize**: Change app name, colors, branding
3. **Deploy**: Build release APK for Play Store
4. **Extend**: Add new features as needed

---

## 💡 Tips & Tricks

### Faster Emulator:
- Use **Android 11 or 12** (newer is slower)
- Enable **"Use Host GPU"** in emulator settings
- Allocate **4GB+ RAM** to emulator

### Firebase Testing:
- Use **Test Mode** for development
- Implement **Security Rules** before production
- Enable **Multi-factor authentication** in production

### Debug Mode:
- Use **Logcat** to view app logs
- Set **Breakpoints** in Android Studio
- Use **Android Profiler** to check performance

---

## 🎓 Learn More

- Android Studio: https://developer.android.com/studio/intro
- Gradle: https://gradle.org/
- Firebase: https://firebase.google.com/docs
- Kotlin: https://kotlinlang.org/docs/
- Jetpack Compose: https://developer.android.com/jetpack/compose

---

## 📞 Getting Help

1. **Check Logs**: `adb logcat -s Aegis`
2. **Read Documentation**: See SETUP.md and COMPLETE_GUIDE.md
3. **Firebase Console**: Check project settings
4. **GitHub Issues**: Report bugs

---

<div align="center">

## 🎉 You're Ready!

Now you have a complete, working Aegis app running locally.

Next: Customize it, test it, deploy it! 🚀

**Your Family. Protected. Always. 🛡️**

</div>
