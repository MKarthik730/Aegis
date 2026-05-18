# 🛡️ Aegis - Family Safety & Emergency Response Platform

A production-ready Android application built with **Kotlin + Jetpack Compose + Firebase**, providing families with real-time protection through intelligent emergency response, continuous location tracking, and comprehensive geofencing.

---

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Setup & Installation](#setup--installation)
- [Building & Running](#building--running)
- [Docker Deployment](#docker-deployment)
- [Firebase Configuration](#firebase-configuration)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## ✨ Features

### 🚨 Emergency Response
- **Multi-trigger SOS**: One-tap, auto-crash, shake, volume button, power button
- **Crash Detection**: 3.5G threshold with 300ms confirmation window
- **Fall Detection**: Freefall + impact pattern recognition
- **Auto-SOS Countdown**: 30-second countdown with manual cancel option
- **Fatigue Detection**: ML Kit face detection for drowsiness

### 📍 Location Services
- **Real-time Tracking**: Active (5s/3m) & Passive (30s/20m) modes
- **Offline Queue**: Room DB persistence for network outages
- **Route Deviation**: 200m+ alerts
- **Speed Monitoring**: Abnormal speed detection
- **Safe Zone Geofencing**: Entry/exit notifications with 150m radius
- **Home WiFi Detection**: SSID-based home arrival

### 👨‍👩‍👧 Family Management
- **Family Groups**: Invite-based group creation
- **Real-time Status**: Online/offline/safe/unsafe indicators
- **Member Profiles**: Name, phone, location, status
- **Role-based Access**: Admin and member permissions

### 🔔 Communication
- **Push Notifications**: FCM with high-priority delivery
- **SMS Fallback**: Offline SOS via SMS
- **Instant Alerts**: Network bypass with DND override
- **Alert History**: Complete log of all safety events

---

## 🏗️ Architecture

**Clean Architecture + MVVM Pattern**

```
┌─────────────────────────────────────────┐
│           UI Layer                       │
│   (Compose Screens + ViewModels)        │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│        Domain Layer                      │
│   (Repositories + Use Cases)            │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Data Layer                       │
│  (Firebase + Room DB)                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Platform Layer                      │
│ (Services + Receivers + System APIs)    │
└─────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Kotlin | 1.9.20 |
| **UI** | Jetpack Compose | 1.6.2 |
| **Architecture** | MVVM + Clean Architecture | - |
| **DI** | Hilt | 2.48.1 |
| **Database** | Room | 2.6.1 |
| **Backend** | Firebase (Auth, RTDB, Firestore, FCM) | Latest |
| **Location** | Google Play Services | 21.1.0 |
| **ML** | ML Kit (Face Detection) | 16.1.5 |
| **Camera** | CameraX | 1.3.0 |
| **Build** | Gradle | 8.2.0 |
| **Min SDK** | 26 (Android 8) | - |
| **Target SDK** | 34 (Android 14) | - |

---

## 🚀 Setup & Installation

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17+
- **Gradle**: 8.2.0+
- **Firebase Account**: With project created
- **Google Cloud Console**: Maps API key

### Step 1: Clone & Setup

```bash
git clone https://github.com/MKarthik730/aegis.git
cd aegis
```

### Step 2: Firebase Configuration

1. Create Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
2. Add Android app with package `com.karthik.aegis`
3. Download `google-services.json` → place in `/app/`
4. Enable:
   - Authentication (Phone + Email)
   - Realtime Database
   - Cloud Firestore
   - Cloud Messaging

### Step 3: Maps API Configuration

```properties
# gradle.properties
MAPS_API_KEY=your_actual_api_key_here
```

Get API key from [Google Cloud Console](https://console.cloud.google.com)

### Step 4: Build Project

```bash
./gradlew clean assembleDebug
```

---

## 🏃 Building & Running

### From Android Studio

1. Open project in Android Studio
2. Sync Gradle files
3. Run on emulator or physical device

### From Command Line

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing key)
./gradlew assembleRelease

# Run on connected device
./gradlew installDebug

# View logs
adb logcat -s Aegis
```

### Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
docker build -t aegis:latest .
```

### Run with Docker Compose

```bash
docker-compose up --build
```

### Extract Built APK

```bash
docker-compose exec aegis-builder sh -c "cp /app/*.apk /app/output/"
```

The built APK will be available in `./build-output/`

---

## 🔥 Firebase Configuration

### Database Structure

```
aegis-project/
├── sos_alerts/
│   └── {uid}/
│       └── reason, latitude, longitude, timestamp, status
├── live_locations/
│   └── {uid}/
│       └── latitude, longitude, speed, accuracy, mode
├── safe_zones/
│   └── {uid}/
│       └── {zoneId}/ name, lat, lng, radius, type
├── family_groups/
│   └── {groupId}/
│       ├── name
│       └── members/ {uid}/ profile, role
├── emergency_contacts/
│   └── {uid}/
│       └── {contactId}/ name, phone, relation, isPrimary
└── fcm_queue/
    └── {messageId}/ title, body, tokens
```

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /fcm_queue/{doc=**} {
      allow read, write: if request.auth != null;
    }
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth.uid == uid;
    }
  }
}
```

---

## 📂 Project Structure

```
Aegis/
├── app/
│   ├── src/main/
│   │   ├── java/com/karthik/aegis/
│   │   │   ├── AegisApplication.kt          # Hilt app entry
│   │   │   ├── di/
│   │   │   │   └── AppModule.kt             # DI configuration
│   │   │   ├── model/
│   │   │   │   └── Models.kt                # Data classes
│   │   │   ├── data/local/
│   │   │   │   ├── AppDatabase.kt           # Room DB
│   │   │   │   └── dao/
│   │   │   ├── repository/
│   │   │   │   ├── SOSRepository.kt
│   │   │   │   ├── ContactsRepository.kt
│   │   │   │   ├── FamilyRepository.kt
│   │   │   │   ├── LocationRepository.kt
│   │   │   │   └── ZoneRepository.kt
│   │   │   ├── service/
│   │   │   │   ├── LocationTrackingService.kt
│   │   │   │   ├── AccidentDetectorService.kt
│   │   │   │   ├── SOSBroadcastReceiver.kt
│   │   │   │   ├── BootReceiver.kt
│   │   │   │   └── AegisFirebaseMessagingService.kt
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── NavHost.kt
│   │   │   │   ├── home/
│   │   │   │   ├── auth/
│   │   │   │   ├── sos/
│   │   │   │   ├── contacts/
│   │   │   │   ├── splash/
│   │   │   │   └── theme/
│   │   │   ├── viewmodel/
│   │   │   │   ├── AuthViewModel.kt
│   │   │   │   └── HomeViewModel.kt
│   │   │   └── utils/
│   │   │       ├── AegisPrefs.kt
│   │   │       ├── DistanceUtils.kt
│   │   │       └── NotificationUtils.kt
│   │   ├── res/
│   │   │   ├── values/strings.xml
│   │   │   └── drawable/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
└── README.md
```

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 🙋 Support

For issues, questions, or suggestions, please open a [GitHub Issue](https://github.com/MKarthik730/aegis/issues).

---

<div align="center">

**Built with ❤️ using Kotlin, Jetpack Compose, and Firebase**

*Your family. Protected. Always.*

</div>
