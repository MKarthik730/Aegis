#                                  🛡️ Aegis
### Real-time Family Safety & Emergency Response Platform
<div align="center">

<img src="https://img.shields.io/badge/-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
<img src="https://img.shields.io/badge/-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
<img src="https://img.shields.io/badge/-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white"/>
<img src="https://img.shields.io/badge/-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black"/>
<img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge"/>
<img src="https://img.shields.io/badge/minSdk-26-green?style=for-the-badge"/>


*Your family. Protected. Always.*

</div>

---

## 📋 Overview

Aegis is a production-ready Android application built with **Clean Architecture + MVVM**, providing families with round-the-clock protection. From instant SOS alerts triggered by accidents or falls, to continuous location sharing and intelligent geofencing — Aegis ensures your loved ones are protected at all times, even offline.

| | |
|---|---|
| 🚨 **Instant Emergency Response** | Multi-trigger SOS with automatic crash & fall detection |
| 📍 **Real-time Location Sharing** | Continuous GPS tracking with offline queue support |
| 🔵 **Intelligent Geofencing** | Customizable safe zones with instant entry/exit notifications |
| 👨‍👩‍👧 **Family Coordination** | Group management with role-based permissions |
| 📵 **Offline Resilience** | SMS fallback when internet is unavailable |

---

## ✨ Key Features

### 🚨 Emergency Response

| Feature | Description |
|---------|-------------|
| **SOS Alert** | One-tap emergency alert with live GPS location to all emergency contacts |
| **Auto Crash Detection** | Accelerometer-based collision detection (3.5G threshold) with 30s countdown |
| **Fall Detection** | Freefall pattern recognition using accelerometer analysis |
| **Fatigue Detection** | ML Kit-powered driver drowsiness detection via front camera |
| **Shake SOS** | Rapid device shaking (5× in 2s) triggers emergency alert |
| **Volume Button SOS** | Triple volume press triggers silent emergency |
| **Power Button SOS** | Five rapid power button presses triggers emergency |
| **Offline SMS** | Automatic SMS fallback when no internet connectivity |

### 📍 Location Services

| Feature | Description |
|---------|-------------|
| **Live Tracking** | Real-time GPS sharing — every 5s active (3m delta) / 30s passive (20m delta) |
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
| **Home Detection** | WiFi SSID-based home arrival detection |
| **School/Work Zones** | Specialized notifications for routine locations |
| **Firebase Sync** | Real-time zone updates across all family devices |

### 👨‍👩‍👧 Family Management

| Feature | Description |
|---------|-------------|
| **Group Creation** | Private family groups with invite links |
| **Member Status** | Real-time online/offline/safe/unsafe status |
| **Role-based Access** | Admin and member permission levels |
| **Member Profiles** | Name, photo, phone, and contact information |

### 📱 Communication & Device Health

| Feature | Description |
|---------|-------------|
| **Push Notifications** | Firebase Cloud Messaging for instant alerts (bypasses DND) |
| **Emergency Call** | One-tap calling to primary emergency contact |
| **Alert History** | Complete log of all safety events |
| **Morse Vibration** | SOS pattern vibration (··· — ···) for incoming alerts |
| **Battery Alerts** | Family notification when battery critically low |
| **Signal Loss Alerts** | Immediate warning when phone goes offline |
| **Night Mode** | Enhanced sensitivity during late-night hours |

---

## 🏗️ Architecture

Aegis follows **Clean Architecture** with strict layer separation. Each layer communicates only with the one directly below it — the UI never touches Firebase directly.

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│   Compose Screens  │  ViewModels (StateFlow)  │  NavHost   │
└───────────────────────────────┬─────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────┐
│                      Domain Layer                           │
│      Repositories  │  Data Models  │  Use Cases            │
└───────────────────────────────┬─────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────┐
│                       Data Layer                            │
│   Firebase (Auth / RTDB / Firestore / FCM)  │  Room / DS   │
└───────────────────────────────┬─────────────────────────────┘
                                │
┌───────────────────────────────▼─────────────────────────────┐
│                     Platform Layer                          │
│     Foreground Services  │  Receivers  │  System APIs       │
└─────────────────────────────────────────────────────────────┘
```

### 🚨 SOS Emergency Flow

```
User triggers SOS
       │
       ├──── Manual (SOS button)
       ├──── Auto Crash (Accel > 3.5G)
       ├──── Shake (5× in 2s)
       ├──── Volume Button (3× press)
       └──── Power Button (5× press)
                    │
                    ▼
          ┌─────────────────┐
          │  30s Countdown  │
          └────────┬────────┘
                   │
          ┌────────┴────────┐
          │                 │
       Cancelled        Not Cancelled
          │                 │
      ✓ Done          Send SOS Alert
                           │
                  ┌────────┴────────┐
                  │                 │
            Firebase OK         Offline
                  │                 │
          Push to RTDB        SMS Fallback
                  │                 │
                  └────────┬────────┘
                           │
               FCM → Contacts receive alert
                           │
                  Real-time location shared
                           │
               ┌───────────┴───────────┐
               │                       │
          "I'm Safe"            Mark RESOLVED
```

### 📍 Location Tracking Flow

```
App Launched
     │
     ▼
Request Location Permissions
     │
  Granted? ──── No ──▶ Show rationale & re-prompt
     │
    Yes
     │
     ▼
Start Foreground Service
     │
     ▼
FusedLocationProvider
     │
  ┌──┴──┐
Active  Passive
5s/3m  30s/20m
  └──┬──┘
     │
     ▼
New location received
     │
  Network? ── No ──▶ Queue in Room DB ──▶ Flush when online
     │                                          │
    Yes                                         │
     │                                          │
     ▼                                          │
Sync to Firebase RTDB ◀─────────────────────────┘
     │
  Check Safe Zones
     │
  ┌──┴──┐
Entered  Exited
  │        │
Notify   Notify
  └──┬──┘
     │
  Route set? ── Yes ──▶ Check deviation > 200m ──▶ Alert
     │
    No
     │
Continue monitoring ──▶ (loop)
```

---

## 🛠️ Technology Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **Language** | Kotlin 1.9.x | Primary development language |
| **UI Framework** | Jetpack Compose | Modern declarative UI toolkit |
| **Design System** | Material Design 3 | Consistent and accessible design |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns |
| **DI** | Hilt | Compile-time DI framework |
| **Authentication** | Firebase Auth | Phone & Email authentication |
| **Realtime Database** | Firebase RTDB | Live location & alert sync |
| **Document Database** | Cloud Firestore | User data & FCM queue |
| **Push Notifications** | Firebase FCM | Instant push delivery |
| **Maps** | Google Maps SDK | Location visualization |
| **Location** | FusedLocationProvider | Battery-efficient GPS |
| **Sensors** | SensorManager | Accelerometer, Gyroscope |
| **Machine Learning** | ML Kit Face Detection | Fatigue monitoring |
| **Camera** | CameraX | Camera integration |
| **Background** | ForegroundService | Persistent tracking |
| **Local Storage** | Room Database | Offline queue |
| **Preferences** | DataStore | App settings |

---

## 📂 Project Structure

```
Aegis/
├── app/src/main/
│   ├── java/com/karthik/aegis/
│   │   ├── AegisApplication.kt              # Hilt Application entry point
│   │   ├── di/
│   │   │   └── AppModule.kt                 # Dependency Injection (Hilt + Room)
│   │   ├── model/
│   │   │   └── Models.kt                    # Data models with Room entities
│   │   ├── data/
│   │   │   └── local/
│   │   │       ├── AppDatabase.kt           # Room database definition
│   │   │       └── dao/
│   │   │           ├── OfflineLocationDao.kt
│   │   │           ├── AlertHistoryDao.kt
│   │   │           └── SafetyScoreDao.kt
│   │   ├── repository/
│   │   │   ├── SOSRepository.kt
│   │   │   ├── ContactsRepository.kt
│   │   │   ├── FamilyRepository.kt
│   │   │   ├── LocationRepository.kt
│   │   │   └── ZoneRepository.kt
│   │   ├── service/
│   │   │   ├── LocationTrackingService.kt
│   │   │   ├── AccidentDetectorService.kt
│   │   │   ├── SOSBroadcastReceiver.kt
│   │   │   ├── BootReceiver.kt
│   │   │   └── AegisFirebaseMessagingService.kt
│   │   ├── ui/
│   │   │   ├── MainActivity.kt
│   │   │   ├── navigation/NavHost.kt
│   │   │   ├── splash/SplashScreen.kt
│   │   │   ├── auth/AuthScreen.kt
│   │   │   ├── home/HomeScreen.kt
│   │   │   ├── contacts/
│   │   │   │   ├── ContactsScreen.kt
│   │   │   │   └── ContactsViewModel.kt
│   │   │   └── theme/Theme.kt
│   │   └── utils/
│   │       ├── AegisPrefs.kt
│   │       ├── DistanceUtils.kt
│   │       └── NotificationUtils.kt
│   ├── res/                                 # Android resources
│   └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
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

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/MKarthik730/aegis.git
cd aegis
```

**2. Create Firebase Project**
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a project named **Aegis**
3. Enable: Authentication (Phone + Email), Realtime Database, Cloud Firestore, Cloud Messaging
4. Add Android app with package `com.karthik.aegis`
5. Download `google-services.json` → place in `/app/`

**3. Configure Google Maps API**
```properties
# gradle.properties
MAPS_API_KEY=your_actual_api_key_here
```
Enable Maps SDK for Android in [Google Cloud Console](https://console.cloud.google.com) and create an API key.

**4. Build the project**
```bash
./gradlew assembleDebug
```

---

## ⚙️ Permissions

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | GPS location tracking |
| `FOREGROUND_SERVICE` | Persistent background services |
| `CAMERA` | ML Kit face detection for fatigue monitoring |
| `POST_NOTIFICATIONS` | Push notification display |
| `SEND_SMS` | Offline SOS via SMS fallback |
| `RECEIVE_BOOT_COMPLETED` | Auto-start services on device boot |

---

## 🗺️ Roadmap

| Phase | Status | Features |
|-------|--------|----------|
| **Phase 1 — Core** | ✅ Complete | SOS, Location tracking, Crash detection, Geofencing |
| **Phase 2 — Intelligence** | 🔄 In Progress | AI Threat Detection, Safety Score engine |
| **Phase 3 — Connectivity** | 📋 Planned | Bluetooth Mesh, Wi-Fi Direct |
| **Phase 4 — UX** | 📋 Planned | Route Playback, Voice SOS, Widgets |
| **Phase 5 — Release** | 📋 Planned | Beta Testing, Play Store submission |

---

## 🤝 Contributing

1. Fork the repository
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built by [Karthik Motupalli](https://github.com/MKarthik730)**
B.Tech CSE · ANITS, Visakhapatnam

*Built with ❤️ using Kotlin, Jetpack Compose, and Firebase*

</div>
