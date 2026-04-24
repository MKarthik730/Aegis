# 🛡️ Aegis
## Real-time Family Safety & Emergency Response Platform

[![Android](https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?style=for-the-badge)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?style=for-the-badge&logo=firebase)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![Stars](https://img.shields.io/github/stars/MKarthik730/aegis?style=for-the-badge)](https://github.com/MKarthik730/aegis/stargazers)

Aegis is a comprehensive Android application designed to provide real-time safety monitoring and emergency response capabilities for families. From instant SOS alerts triggered by accidents or falls to continuous location sharing and intelligent geofencing, Aegis ensures your loved ones are protected around the clock.

---

## 📋 Overview

Aegis is a production-ready Android application built with modern development practices, providing families with:

- **Instant Emergency Response** — Multi-trigger SOS system with automatic crash and fall detection
- **Real-time Location Sharing** — Continuous GPS tracking with offline queue support
- **Intelligent Geofencing** — Customizable safe zones with instant entry/exit notifications
- **Family Coordination** — Group management with role-based permissions and member status monitoring
- **Offline Resilience** — SMS fallback when internet is unavailable

---

## ✨ Key Features

### 🚨 Emergency Response

| Feature | Description |
|---------|-------------|
| **SOS Alert** | One-tap emergency alert with live GPS location to all emergency contacts |
| **Auto Crash Detection** | Accelerometer-based collision detection (3.5G threshold) with 30s countdown |
| **Fall Detection** | Freefall pattern recognition using accelerometer analysis |
| **Fatigue Detection** | ML Kit-powered driver drowsiness detection via front camera |
| **Shake SOS** | Rapid device shaking (5x in 2s) triggers emergency alert |
| **Volume Button SOS** | Triple volume press triggers silent emergency |
| **Power Button SOS** | Five rapid power button presses triggers emergency |
| **Offline SMS** | Automatic SMS fallback when no internet connectivity |

### 📍 Location Services

| Feature | Description |
|---------|-------------|
| **Live Tracking** | Real-time GPS sharing with configurable intervals (5s active / 30s passive) |
| **Route Deviation** | Alerts when user deviates 200m+ from planned route |
| **Speed Monitoring** | Immediate alerts for abnormal speed detection |
| **Location History** | Full movement history stored for review |
| **Offline Queue** | Room DB-based location storage during network outages |
| **ETA Tracking** | Estimated arrival time calculation |

### 🔵 Geofencing

| Feature | Description |
|---------|-------------|
| **Safe Zones** | Custom radius zones (default 150m) with entry/exit notifications |
| **Danger Zones** | Unsafe area alerts |
| **Home Detection** | WiFi-based home arrival detection |
| **School/Work Zones** | Specialized notifications for routine locations |
| **Firebase Sync** | Real-time zone updates across all family devices |

### 👨‍👩‍👧 Family Management

| Feature | Description |
|---------|-------------|
| **Group Creation** | Private family groups with invite links |
| **Member Status** | Real-time online/offline/safe/unsafe status |
| **Role-based Access** | Admin and member permission levels |
| **Member Profiles** | Name, photo, phone, and contact information |

### 📱 Communication

| Feature | Description |
|---------|-------------|
| **Push Notifications** | Firebase Cloud Messaging for instant alerts |
| **Emergency Call** | One-tap calling to primary emergency contact |
| **Alert History** | Complete log of all safety events |
| **Morse Vibration** | SOS pattern vibration (... --- ...) for incoming alerts |

### 🔧 Device Health

| Feature | Description |
|---------|-------------|
| **Battery Alerts** | Family notification when battery critically low |
| **Signal Loss Alerts** | Immediate warning when phone goes offline |
| **DND Override** | All safety alerts bypass Do Not Disturb mode |
| **Night Mode** | Enhanced sensitivity during late night hours |

---

## 🛠️ Technology Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **Language** | Kotlin 1.9.x | Primary development language |
| **UI Framework** | Jetpack Compose | Modern declarative UI toolkit |
| **Design System** | Material Design 3 | Consistent and accessible design |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns |
| **Dependency Injection** | Hilt | Compile-time DI framework |
| **Authentication** | Firebase Auth | Phone & Email authentication |
| **Real-time Database** | Firebase Realtime DB | Live location & alert sync |
| **Document Database** | Cloud Firestore | User data & FCM queue |
| **Push Notifications** | Firebase Cloud Messaging | Instant notifications |
| **Maps** | Google Maps SDK | Location visualization |
| **Location Services** | FusedLocationProvider | Battery-efficient GPS |
| **Sensors** | SensorManager | Accelerometer, Gyroscope |
| **Machine Learning** | ML Kit Face Detection | Fatigue monitoring |
| **Camera** | CameraX | Camera integration |
| **Background Processing** | ForegroundService | Persistent tracking |
| **Local Storage** | Room Database | Offline queue |
| **Preferences** | DataStore | App settings |

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────┐
│                         UI Layer                           │
│   Screens (Compose)  │  ViewModels (StateFlow)  │  Nav    │
└───────────────────────────────┬────────────────────────────┘
                                │
┌────────────────────────────────────────────────────────────┐
│                      Domain Layer                          │
│   Repositories  │  Models (Data)  │  Use Cases (Logic)     │
└───────────────────────────────┬────────────���───────────────┘
                                │
┌────────────────────────────────────────────────────────────┐
│                       Data Layer                           │
│   Firebase (Auth/DB/Firestore/FCM)  │  Local (Room/DStore)│
└───────────────────────────────┬────────────────────────────┘
                                │
┌────────────────────────────────────────────────────────────┐
│                    Platform Layer                          │
│   Services (Foreground)  │  Receivers  │  System APIs      │
└────────────────────────────────────────────────────────────┘
```

---

## 📂 Project Structure

```
Aegis/
├── app/src/main/
│   ├── java/com/karthik/aegis/
│   │   ├── AegisApplication.kt     # Hilt Application entry point
│   │   ├── di/AppModule.kt         # Dependency Injection
│   │   ├── model/Models.kt         # Data models
│   │   ├── repository/             # Repositories
│   │   │   ├── SOSRepository.kt
│   │   │   ├── ContactsRepository.kt
│   │   │   ├── FamilyRepository.kt
│   │   │   ├── LocationRepository.kt
│   │   │   └── ZoneRepository.kt
│   │   ├── service/                # Background services
│   │   │   ├── LocationTrackingService.kt
│   │   │   ├── AccidentDetectorService.kt
│   │   │   ├── SOSBroadcastReceiver.kt
│   │   │   ├── BootReceiver.kt
│   │   │   └── AegisFirebaseMessagingService.kt
│   │   ├── ui/                     # User interface
│   │   │   ├── MainActivity.kt
│   │   │   ├── navigation/NavHost.kt
│   │   │   ├── splash/SplashScreen.kt
│   │   │   ├── auth/AuthScreen.kt
│   │   │   ├���─ home/HomeScreen.kt
│   │   │   └── theme/Theme.kt
│   │   └── utils/                  # Utilities
│   │       ├── AegisPrefs.kt
│   │       ├── DistanceUtils.kt
│   │       └── NotificationUtils.kt
│   ├── res/                        # Android resources
│   └── AndroidManifest.xml
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Project settings
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Version |
|-------------|---------|
| Android Studio | Hedgehog (2023.1.1) or later |
| Java Development Kit | JDK 17 |
| Android SDK | API 26+ (minSdk) |
| Target SDK | API 34 |

### Installation Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/MKarthik730/aegis.git
cd aegis
```

#### 2. Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create project "Aegis"
3. Enable: Authentication (Phone + Email), Realtime Database, Cloud Firestore, Cloud Messaging
4. Add Android app with package `com.karthik.aegis`
5. Download `google-services.json` to `/app/`

#### 3. Configure Google Maps API
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Enable Maps SDK for Android
3. Create API Key and add to `gradle.properties`:
```properties
MAPS_API_KEY=your_actual_api_key_here
```

#### 4. Build the Project
```bash
./gradlew assembleDebug
```

---

## ⚙️ Permissions

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | GPS location tracking |
| `FOREGROUND_SERVICE` | Persistent background services |
| `CAMERA` | ML Kit face detection |
| `POST_NOTIFICATIONS` | Push notification display |
| `SEND_SMS` | Offline SOS via SMS |
| `RECEIVE_BOOT_COMPLETED` | Auto-start on device boot |

---

## 🗺️ Roadmap

| Phase | Status | Features |
|-------|--------|----------|
| Phase 1 — Core | ✅ Complete | SOS, Location, Crash Detection, Geofencing |
| Phase 2 — Intelligence | 🔄 In Progress | AI Threat Detection, Safety Score |
| Phase 3 — Connectivity | 📋 Planned | Bluetooth Mesh, Wi-Fi Direct |
| Phase 4 — UX | 📋 Planned | Route Playback, Voice SOS, Widgets |
| Phase 5 — Release | 📋 Planned | Beta Testing, Play Store |

---

## 🤝 Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feature/your-feature`
3. Make changes and commit
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

MIT License - See [LICENSE](LICENSE) file for details.

---

## 👤 Contact

**Karthik** — [@MKarthik730](https://github.com/MKarthik730)  
B.Tech CSE | ANITS, Visakhapatnam

---

*Built with ❤️ using Kotlin, Jetpack Compose, and Firebase*  
*Your family. Protected. Always.*