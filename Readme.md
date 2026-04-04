<div align="center">

# 🛡️ Aegis
### Real-time Family Safety & Emergency Alert App

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)](https://firebase.google.com)
[![Maps](https://img.shields.io/badge/Maps-Google%20Maps%20SDK-blue?logo=googlemaps)](https://developers.google.com/maps)
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
| Feature | Description |
|---|---|
| SOS Alert | One tap sends emergency alert with live location instantly |
| Silent SOS | Secret alert triggered by holding volume down three seconds |
| Auto SOS | Automatically alerts family if check-in is not responded |
| Crash Detection | Detects high-impact collision and triggers emergency alert automatically |
| Fall Detection | Monitors sensors for serious fall and alerts your family |
| Panic Button | Large always-visible button fires SOS without opening app |
| Voice Triggered SOS | Say trigger phrase to fire SOS without touching phone |
| Scheduled SOS Check | Auto-alerts family if you miss a scheduled safety check |
| Impact Drop Detection | Detects phone freefall and high-altitude drop automatically |
| Sudden Deceleration Alert | Detects vehicle crashing from high speed to sudden stop |

### 📍 Location
| Feature | Description |
|---|---|
| Live Location | Shares your real-time GPS location with family continuously |
| Location History | Stores your past movement data for later review |
| Route Playback | Replay any family member's route from the past day |
| Last Seen Location | Shows exact location where member was last active online |
| Speed Monitoring | Detects abnormally high speed and alerts family immediately |
| Live ETA | Shows estimated arrival time to your set destination live |
| Arrival Alerts | Notifies family the moment you reach your destination safely |
| Departure Alerts | Notifies family the moment you leave a known location |

### 🔵 Zones
| Feature | Description |
|---|---|
| Safe Zones | Define trusted areas like home or school for monitoring |
| Danger Zones | Mark unsafe areas and get alerted when nearby |
| School Zone Alert | Special alert when child enters or leaves school zone |
| Work Zone Alert | Notifies family when member arrives or leaves workplace |
| Custom Zone Names | Name your zones anything meaningful to your family |
| Zone Entry Alert | Instant notification when any member enters a defined zone |
| Zone Exit Alert | Instant notification when any member leaves a defined zone |

### 👨‍👩‍👧 Family
| Feature | Description |
|---|---|
| Family Group | Create a private group connecting all your family members |
| Family Invite | Invite members via shareable link in one tap |
| Member Status | See if each family member is safe or unsafe |
| Member Profile | Each member has a name, photo, and contact info |
| Trust Levels | Assign admin or member roles within your family group |
| Admin Controls | Admin can manage zones, contacts, and group settings |

### 💬 Communication
| Feature | Description |
|---|---|
| In-app Messaging | Send quick messages to family directly inside Aegis |
| Emergency Call Shortcut | One tap calls your primary emergency contact instantly |
| Offline SMS Fallback | Sends SOS via SMS when there is no internet |
| Real-time Notifications | Instant push alerts for every safety event and update |
| Alert History Log | Full history of every alert triggered within your group |

### 🔋 Device & Health
| Feature | Description |
|---|---|
| Battery Alert | Warns family when your battery drops below critical level |
| Phone Signal Alert | Alerts family when your phone loses network signal entirely |
| Night Mode Sensitivity | Increases detection sensitivity automatically during late night hours |
| Do Not Disturb Override | Safety alerts bypass DND and silent mode always |
| Device Offline Alert | Notifies family when your device goes completely offline unexpectedly |

### 🧠 Intelligence
| Feature | Description |
|---|---|
| Safety Score | Weekly score based on check-ins, zones, and safe behavior |
| Threat Detection | AI detects unusual patterns like stopped movement or odd location |
| Unusual Activity Alert | Flags abnormal behavior like no movement for too long |
| Weekly Safety Report | Summary of your family's safety activity every week |
| Mesh Network Fallback | Devices communicate over Bluetooth when internet and SMS fail |

---

## 🧰 Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Repository Pattern
- **Auth:** Firebase Authentication
- **Database:** Firebase Realtime DB + Firestore
- **Notifications:** Firebase Cloud Messaging (FCM)
- **Maps:** Google Maps SDK + Geofencing API
- **Location:** FusedLocationProviderClient
- **Sensors:** Android SensorManager (Accelerometer + Gyroscope + Barometer)
- **Background:** ForegroundService + WorkManager

---

## 🏗️ Architecture

```
UI (Jetpack Compose)
      ↓
ViewModel (StateFlow / LiveData)
      ↓
Repository
      ↓
Firebase (Auth / Firestore / Realtime DB / FCM)
Google Maps SDK / SensorManager
```

---

## 📁 Project Structure

```
Aegis/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/com/karthik/aegis/
│           │   ├── ui/
│           │   │   ├── splash/
│           │   │   ├── auth/
│           │   │   ├── home/
│           │   │   ├── sos/
│           │   │   ├── contacts/
│           │   │   └── zones/
│           │   ├── viewmodel/
│           │   ├── repository/
│           │   ├── model/
│           │   ├── service/
│           │   │   ├── LocationTrackingService.kt
│           │   │   ├── AccidentDetectorService.kt
│           │   │   └── SOSBroadcastReceiver.kt
│           │   └── utils/
│           ├── res/
│           └── AndroidManifest.xml
│
├── screenshots/
├── .gitignore
├── build.gradle
└── README.md
```

---

##  Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+
- Google account (for Firebase + Maps API)

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/MKarthik730/aegis.git
   cd aegis
   ```

2. **Create a Firebase project** at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable Authentication (Phone / Email)
   - Enable Realtime Database
   - Enable Firestore
   - Enable Cloud Messaging (FCM)

3. **Add Firebase config** — Download `google-services.json` and place it inside `/app`

4. **Add Maps API key** — Get one from [Google Cloud Console](https://console.cloud.google.com) and add to `local.properties`:
   ```
   MAPS_API_KEY=your_api_key_here
   ```

5. **Build and run** on a physical device or emulator (API 26+)

> ⚠️ Never commit `google-services.json` or `local.properties` — both are in `.gitignore`

---

## 📸 Screenshots

| Home Map | SOS Screen | Safe Zones | Family Group |
|---|---|---|---|
| coming soon | coming soon | coming soon | coming soon |

---

## 🗺️ Roadmap

- [x] Project scaffold & README
- [ ] Firebase Auth + Family group creation
- [ ] Real-time location sharing on map
- [ ] SOS alert system
- [ ] Crash & fall detection
- [ ] Geofencing / Safe zones
- [ ] Silent SOS & Panic Button
- [ ] Battery & signal alerts
- [ ] AI threat detection
- [ ] Mesh network fallback
- [ ] Play Store release

---

## 🤝 Contributing

Pull requests are welcome! For major changes, please open an issue first to discuss what you'd like to change.

---

## 👨‍💻 Author

**Karthik** — [@MKarthik730](https://github.com/MKarthik730)
B.Tech CSE | ANITS, Visakhapatnam
ML Enthusiast | Open Source Contributor

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
