<div align="center">

# 🛡️ Aegis
### Real-time Safety & Emergency Alert App

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)](https://firebase.google.com)
[![Maps](https://img.shields.io/badge/Maps-Google%20Maps%20SDK-blue?logo=googlemaps)](https://developers.google.com/maps)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-red)](https://dagger.dev/hilt)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)
[![Status](https://img.shields.io/badge/Status-In%20Development-yellow)]()

*Your family. Protected. Always.*

</div>

---

## 📱 What is Aegis?

Aegis is an all-in-one Android family safety app that keeps your loved ones protected in real time. From one-tap SOS alerts and crash detection to live location sharing and smart geofencing — Aegis is the shield your family never knew they needed.

---

## ✨ Features

### ⚡ Safety & Alerts
| Feature | Status | Description |
|---|---|---|
| SOS Alert | ✅ | One tap sends emergency alert with live location instantly |
| Silent SOS | ✅ | Secret alert triggered by holding volume down three seconds |
| Auto SOS | ✅ | Automatically alerts family if check-in is not responded |
| Crash Detection | ✅ | Detects high-impact collision and triggers emergency alert automatically |
| Fall Detection | ✅ | Monitors sensors for serious fall and alerts your family |
| Panic Button | ✅ | Large always-visible button fires SOS without opening app |
| Voice Triggered SOS | 🔜 | Say trigger phrase to fire SOS without touching phone |
| Scheduled SOS Check | 🔜 | Auto-alerts family if you miss a scheduled safety check |
| Impact Drop Detection | ✅ | Detects phone freefall and high-altitude drop automatically |
| Sudden Deceleration Alert | ✅ | Detects vehicle crashing from high speed to sudden stop |

### 📍 Location
| Feature | Status | Description |
|---|---|---|
| Live Location | ✅ | Shares your real-time GPS location with family continuously |
| Location History | ✅ | Stores your past movement data for later review |
| Route Playback | 🔜 | Replay any family member's route from the past day |
| Last Seen Location | ✅ | Shows exact location where member was last active online |
| Speed Monitoring | ✅ | Detects abnormally high speed and alerts family immediately |
| Live ETA | 🔜 | Shows estimated arrival time to your set destination live |
| Arrival Alerts | ✅ | Notifies family the moment you reach your destination safely |
| Departure Alerts | ✅ | Notifies family the moment you leave a known location |

### 🔵 Zones
| Feature | Status | Description |
|---|---|---|
| Safe Zones | ✅ | Define trusted areas like home or school for monitoring |
| Danger Zones | ✅ | Mark unsafe areas and get alerted when nearby |
| School Zone Alert | ✅ | Special alert when child enters or leaves school zone |
| Work Zone Alert | ✅ | Notifies family when member arrives or leaves workplace |
| Custom Zone Names | ✅ | Name your zones anything meaningful to your family |
| Zone Entry Alert | ✅ | Instant notification when any member enters a defined zone |
| Zone Exit Alert | ✅ | Instant notification when any member leaves a defined zone |

### 👨‍👩‍👧 Family
| Feature | Status | Description |
|---|---|---|
| Family Group | ✅ | Create a private group connecting all your family members |
| Family Invite | ✅ | Invite members via shareable link in one tap |
| Member Status | ✅ | See if each family member is safe or unsafe |
| Member Profile | ✅ | Each member has a name, photo, and contact info |
| Trust Levels | ✅ | Assign admin or member roles within your family group |
| Admin Controls | ✅ | Admin can manage zones, contacts, and group settings |

### 💬 Communication
| Feature | Status | Description |
|---|---|---|
| In-app Messaging | 🔜 | Send quick messages to family directly inside Aegis |
| Emergency Call Shortcut | ✅ | One tap calls your primary emergency contact instantly |
| Offline SMS Fallback | ✅ | Sends SOS via SMS when there is no internet |
| Real-time Notifications | ✅ | Instant push alerts for every safety event and update |
| Alert History Log | ✅ | Full history of every alert triggered within your group |

### 🔋 Device & Health
| Feature | Status | Description |
|---|---|---|
| Battery Alert | ✅ | Warns family when your battery drops below critical level |
| Phone Signal Alert | ✅ | Alerts family when your phone loses network signal entirely |
| Night Mode Sensitivity | ✅ | Increases detection sensitivity automatically during late night hours |
| Do Not Disturb Override | ✅ | Safety alerts bypass DND and silent mode always |
| Device Offline Alert | ✅ | Notifies family when your device goes completely offline unexpectedly |

### 🧠 Intelligence
| Feature | Status | Description |
|---|---|---|
| Safety Score | 🔜 | Weekly score based on check-ins, zones, and safe behavior |
| Threat Detection | 🔜 | AI detects unusual patterns like stopped movement or odd location |
| Unusual Activity Alert | ✅ | Flags abnormal behavior like no movement for too long |
| Weekly Safety Report | 🔜 | Summary of your family's safety activity every week |
| Mesh Network Fallback | 🔜 | Devices communicate over Bluetooth when internet and SMS fail |

**Legend:** ✅ Implemented | 🔜 Planned/In Progress

---

## 🧰 Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin 1.9.x |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Repository Pattern |
| **DI** | Hilt |
| **Auth** | Firebase Authentication (Phone + Email) |
| **Database** | Firebase Realtime DB + Firestore |
| **Notifications** | Firebase Cloud Messaging (FCM) |
| **Maps** | Google Maps SDK + Compose |
| **Location** | FusedLocationProviderClient |
| **Sensors** | Android SensorManager (Accelerometer + Gyroscope) |
| **ML** | ML Kit Face Detection (Fatigue) |
| **Background** | ForegroundService + WorkManager |
| **Storage** | Room DB (offline queue) + DataStore Preferences |
| **Camera** | CameraX |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│            Jetpack Compose + ViewModels                │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Domain Layer                         │
│        Repositories (Location, Family, SOS)           │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Data Layer                           │
│   Firebase (Auth/Firestore/RTDB/FCM) + Room DB        │
│   Location Services + Sensor Manager                  │
└─────────────────────────────────────────────────────────┘
```

---

## 📁 Project Structure

```
Aegis/
│
├── app/
│   └── src/main/
│       ├── java/com/karthik/aegis/
│       │   ├── AegisApplication.kt          # Hilt Application
│       │   ├── di/
│       │   │   └── AppModule.kt             # Dependency Injection
│       │   ├── model/
│       │   │   └── Models.kt                # Data classes
│       │   ├── utils/
│       │   │   ├── AegisPrefs.kt            # DataStore preferences
│       │   │   ├── DistanceUtils.kt         # GPS calculations
│       │   │   └── NotificationUtils.kt    # Notification helpers
│       │   ├── repository/
│       │   │   ├── SOSRepository.kt         # SOS logic
│       │   │   ├── ContactsRepository.kt   # Emergency contacts
│       │   │   ├── LocationRepository.kt   # Location tracking
│       │   │   ├── FamilyRepository.kt      # Family groups
│       │   │   └── ZoneRepository.kt       # Safe zones
│       │   ├── service/
│       │   │   ├── LocationTrackingService.kt  # Background GPS
│       │   │   ├── AccidentDetectorService.kt  # Crash/Fall detection
│       │   │   ├── SOSBroadcastReceiver.kt     # SOS triggers
│       │   │   ├── BootReceiver.kt             # Auto-start
│       │   │   └── AegisFirebaseMessagingService.kt  # Push notifications
│       │   └── ui/
│       │       ├── MainActivity.kt         # Entry point
│       │       ├── navigation/
│       │       │   └── NavHost.kt          # Navigation graph
│       │       ├── theme/
│       │       │   ├── Theme.kt           # Material 3 theme
│       │       │   └── Typography.kt
│       │       ├── splash/
│       │       │   ├── Screen.kt          # Splash ViewModel
│       │       │   └── SplashScreen.kt
│       │       ├── auth/
│       │       │   ├── AuthScreen.kt      # Phone/Email login
│       │       │   └── AuthViewModel.kt
│       │       └── home/
│       │           ├── HomeScreen.kt       # Main dashboard
│       │           └── HomeViewModel.kt
│       │
│       ├── res/
│       │   ├── drawable/
│       │   ├── values/
│       │   └── xml/
│       │
│       ├── AndroidManifest.xml
│       └── build.gradle.kts
│
├── services/                           # Legacy location-aware service files
│   └── *.kt
│
├── sos/                               # Legacy SOS implementation
│   └── *.kt
│
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
│
├── build.gradle.kts                    # Root build file
├── settings.gradle.kts
├── gradle.properties
├── local.properties.example
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26+ (minSdk)
- Android SDK 34 (targetSdk/compileSdk)
- JDK 17
- Google account (for Firebase + Maps API)

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/MKarthik730/aegis.git
   cd aegis
   ```

2. **Create a Firebase project**
   - Go to [console.firebase.google.com](https://console.firebase.google.com)
   - Create new project "Aegis"
   - Enable:
     - **Authentication** (Phone + Email/Password)
     - **Realtime Database** (start in test mode)
     - **Firestore** (start in test mode)
     - **Cloud Messaging** (for push notifications)

3. **Download Firebase config**
   - Go to Project Settings → Your apps → Add Android app
   - Enter package name: `com.karthik.aegis`
   - Download `google-services.json`
   - Replace the placeholder file in `/app`

4. **Get Google Maps API Key**
   - Go to [Google Cloud Console](https://console.cloud.google.com)
   - Enable **Maps SDK for Android**
   - Create API key with restrictions
   - Update `gradle.properties`:
     ```
     MAPS_API_KEY=your_actual_api_key_here
     ```
   - Copy to `local.properties` in project root (if not using gradle.properties)

5. **Open in Android Studio**
   - Open the project folder
   - Wait for Gradle sync to complete
   - Build and run on a physical device (API 26+)

> ⚠️ **Important:** Never commit `google-services.json` or `local.properties` to version control. Both are in `.gitignore`.

---

## 🧪 Features In Detail

### SOS Alert System
- **Multiple triggers:** Button, volume button (5 presses), power button (5 presses), shake, crash detection, fall detection
- **Auto-SOS countdown:** 30-second countdown with cancel option after crash/fall detection
- **Multi-channel alert:** Firebase Realtime DB + FCM push + SMS fallback
- **Audit trail:** All SOS events logged with timestamp and reason

### Location Tracking
- **Active mode:** Updates every 5 seconds (driving)
- **Passive mode:** Updates every 30 seconds (walking/idle)
- **Offline queue:** Room DB stores locations when offline, syncs when back online
- **Anomaly detection:** Alerts if stationary for 30+ minutes on route

### Safe Zones
- **Firebase real-time sync:** Zones update live across devices
- **Geofencing:** Configurable radius (default 150m)
- **Special zones:** Home, School, Work with specific notifications
- **Route deviation:** Alerts if user deviates 200m+ from planned route

### Accident Detection
- **Crash detection:** 3.5G threshold with 300ms confirmation window
- **Fall detection:** Freefall → impact pattern analysis
- **Fatigue detection:** ML Kit face analysis for drowsy driving (camera required)
- **Shake SOS:** 5 shakes in 2 seconds triggers emergency

---

## 📸 Screenshots

| Splash | Auth | Home | SOS |
|---|---|---|---|
| coming soon | coming soon | coming soon | coming soon |

---

## 🗺️ Roadmap

### Phase 1 - Core (Current)
- [x] Project scaffold & README
- [x] Firebase Auth + Family group creation
- [x] Real-time location sharing
- [x] SOS alert system
- [x] Crash & fall detection
- [x] Geofencing / Safe zones
- [x] Silent SOS & Panic Button
- [x] Battery & signal alerts

### Phase 2 - Intelligence
- [ ] AI threat detection
- [ ] Safety Score system
- [ ] Weekly reports

### Phase 3 - Connectivity
- [ ] Mesh network fallback (Bluetooth)
- [ ] Offline SMS via Mesh
- [ ] Wi-Fi Direct SOS

### Phase 4 - Polish
- [ ] Route playback
- [ ] In-app messaging
- [ ] Live ETA
- [ ] Voice triggered SOS

### Release
- [ ] Play Store release
- [ ] Beta testing program

---

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines first.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

For major changes, please open an issue first to discuss what you'd like to change.

---

## 👨‍💻 Author

**Karthik** — [@MKarthik730](https://github.com/MKarthik730)  
B.Tech CSE | ANITS, Visakhapatnam  
ML Enthusiast | Open Source Contributor

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) - see the LICENSE file for details.

---

<div align="center">

Built with ❤️ for family safety

</div>