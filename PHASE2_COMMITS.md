# Phase 2 — Remaining Commits (3–22)

## Completed

- `00a04e0` — feat: add Room entity annotations and column info to data models
- `1f7ab06` — feat: create AppDatabase and DAOs for alerts, scores, and offline queue

---

## 3 — feat: wire Room database and DAOs into Hilt dependency injection

**Files:** `di/AppModule.kt`

- Add `@Provides @Singleton` for `AppDatabase` (build `Room.databaseBuilder`)
- Add `@Provides` for each DAO (`.offlineLocationDao()`, `.alertHistoryDao()`, `.safetyScoreDao()`)
- Add `@Provides` for `Converters` if needed

---

## 4 — feat: implement offline location queue with Room persistence

**Files:** `repository/LocationRepository.kt`

- Inject `OfflineLocationDao` via constructor
- Implement `queueOfflineLocation()` → `dao.insert()`
- Implement `flushOfflineQueue()` → `dao.getPendingLocations()` → sync to Firebase → `dao.deleteByIds()`

---

## 5 — feat: implement emergency contacts screen with CRUD operations

**Files:**
- `ui/contacts/ContactsScreen.kt`
- `ui/contacts/ContactsViewModel.kt`

- LazyColumn of contacts with name, phone, relation chips
- FAB to add contact (bottom sheet form)
- Swipe-to-delete or long-press to remove
- `ContactsViewModel`: CRUD via `ContactsRepository`, `StateFlow<UiState>`

---

## 6 — feat: implement SOS alert screen with countdown and safe flow

**Files:**
- `ui/sos/SOSScreen.kt`
- `ui/sos/SOSViewModel.kt`

- Big red SOS button (180dp circle)
- Confirmation dialog before firing
- Countdown timer (30s → 1s) with cancel option
- "I'm Safe" flow to resolve
- `SOSViewModel`: trigger/resolve via `SOSRepository`, observe status

---

## 7 — feat: implement safe zones screen with map integration

**Files:**
- `ui/zones/ZonesScreen.kt`
- `ui/zones/ZonesViewModel.kt`

- Google Maps Compose view with zone circles
- Long-press to add zone (bottom sheet: name, radius, type, color)
- Tap zone to edit/delete
- `ZonesViewModel`: CRUD via `ZoneRepository`, map camera state

---

## 8 — feat: implement family management screen with invite and status

**Files:**
- `ui/family/FamilyScreen.kt`
- `ui/family/FamilyViewModel.kt`

- Create/join family group
- Member list with real-time status dots (Safe=green, Unsafe=red, Offline=gray)
- Invite via share sheet (deep link with groupId)
- Role badges (Admin/Member)
- `FamilyViewModel`: group CRUD via `FamilyRepository`

---

## 9 — feat: update navigation graph with all screen routes

**Files:** `ui/navigation/NavHost.kt`

- Wire all 4 new screens (SOS, Contacts, Zones, Family) with their ViewModels
- Add `Screen.Safety`, `Screen.Threats`, `Screen.Insights` routes (stubs for now)

---

## 10 — fix: wire HomeViewModel to real repository flows and fix context bug

**Files:** `ui/home/HomeViewModel.kt`

- Replace empty stub flows with real `FamilyRepository.observeFamilyMembers()`, `LocationRepository.observeFamilyLocations()`, `SOSRepository.observeSOSAlerts()`
- Fix `startLocationTracking()` — replace `android.app.Application()` with proper `@ApplicationContext` injection or pass context from Activity
- Inject `AegisPrefs` for user name

---

## 11 — feat: implement PatternLearner for daily routine discovery

**Files:**
- `analytics/PatternLearner.kt`
- `analytics/RoutineTracker.kt`

- Runs daily via WorkManager
- Reads last 7 days of `TrackedLocation` from Room
- Clusters locations (DBSCAN-like: group points within 200m radius, min 3 visits)
- Labels clusters: home (most frequent night-time), work (most frequent weekday 9-5)
- Learns: typical home/work times, frequent routes, activity windows
- Stores patterns in DataStore + Firebase RTDB (`learned_patterns/{uid}/routines`)

---

## 12 — feat: implement AnomalyDetector with location and time-based rules

**Files:** `analytics/AnomalyDetector.kt`

- Consumes live `TrackedLocation` stream
- Rules:
  - Prolonged stationary (>45 min on a planned route)
  - Unusual speed variance (erratic driving: speed changes >30 km/h in <5s)
  - Time-of-day location mismatch (e.g., at unknown location 2 AM on weekday)
  - Signal loss without movement recovery (>30 min offline, last known location unfamiliar)
  - Zone boundary violation (entering danger zone at night)
- Emits `AnomalyEvent` sealed class via `SharedFlow`

---

## 13 — feat: implement ThreatClassifier for severity tier assignment

**Files:** `analytics/ThreatClassifier.kt`

- Input: `AnomalyEvent` from `AnomalyDetector`
- Rules engine assigns tier:
  - `LOW` — minor route deviation, late check-in
  - `MEDIUM` — unfamiliar location, prolonged stationary on route
  - `HIGH` — erratic driving, entering danger zone at night
  - `CRITICAL` — combined anomalies + SOS trigger
- Output: `ThreatEvent` data class with tier, timestamp, location, description, resolved status

---

## 14 — feat: implement ThreatRepository for persistence and Firebase sync

**Files:** `analytics/ThreatRepository.kt`

- Persists `ThreatEvent` to Room (`AlertHistory` table with type="THREAT")
- Syncs to Firebase RTDB (`threat_events/{uid}/{id}`)
- Exposes `Flow<List<ThreatEvent>>` for UI
- Methods: `report(event)`, `resolve(id)`, `observeActive()`, `observeHistory()`

---

## 15 — feat: implement SafetyScoreCalculator with weighted scoring

**Files:** `analytics/SafetyScoreCalculator.kt`

- Weekly 0–100 score from:
  - Check-in rate (30%): actual check-ins / expected check-ins × 30
  - Zone compliance (25%): time spent in safe zones / total time × 25
  - SOS trigger penalty (−15 per false alarm, −5 per genuine)
  - Safe behavior (20%): staying in known zones during routine windows
  - Engagement (10%): app opened at least once/day = 10 points
- Computes at end of each week (Sunday midnight)
- Saves result to `SafetyScoreDao` + Firebase

---

## 16 — feat: implement SafetyScoreRepository with Firebase family sharing

**Files:** `analytics/SafetyScoreRepository.kt`

- Wraps `SafetyScoreDao` + Firebase RTDB
- On new score: save to Room, upload to `safety_scores/{uid}/weekly/{weekStart}`
- On family view: fetch scores for all family UIDs via `getForFamily()`
- Exposes `Flow<SafetyScore>` for current user
- Triggers FCM to family if score drops >20 points from previous week

---

## 17 — feat: add SafetyScoreWorker for weekly background computation

**Files:** `worker/SafetyScoreWorker.kt`

- `CoroutineWorker` with `PeriodicWorkRequest` (7 days, flex interval)
- Gathers data from Room (check-ins, zone logs, SOS history, etc.)
- Computes score via `SafetyScoreCalculator`
- Stores via `SafetyScoreRepository`
- Shows notification with score summary

---

## 18 — feat: add SafetyDashboardScreen with animated score gauge

**Files:**
- `ui/safety/SafetyDashboardScreen.kt`
- `ui/safety/SafetyDashboardViewModel.kt`

- Route: `safety`
- Animated circular gauge (0–100) with color zones (red <50, yellow 50–75, green >75)
- 7-day trend sparkline
- 4 breakdown cards: check-ins, zones, triggers, behavior
- Family scores list (if in a group)
- Pull-to-refresh

---

## 19 — feat: add ThreatCenter and ThreatDetail screens

**Files:**
- `ui/threats/ThreatCenterScreen.kt`
- `ui/threats/ThreatDetailScreen.kt`

- Route: `threats` / `threats/{id}`
- Center: LazyColumn with severity chip (color-coded), timestamp, location snippet. Filter by tier. Swipe to resolve.
- Detail: Map snapshot, full description, timeline, classification breakdown, resolve button.

---

## 20 — feat: add InsightScreen with pattern-based safety insights

**Files:** `ui/insights/InsightScreen.kt`

- Route: `insights`
- AI-generated card feed:
  - "Your safest day is Tuesday"
  - "You visited 2 new locations this week"
  - "Check-in consistency is down 15% from last week"
  - "You usually leave home at 8:30 AM (it's currently 10 AM — deviation)"
- Cards generated from `PatternLearner` data + `SafetyScore` trends

---

## 21 — feat: add insight notification channel and PatternLearningWorker

**Files:**
- `utils/NotificationUtils.kt` — add `CHANNEL_INSIGHTS` (IMPORTANCE_DEFAULT)
- `worker/PatternLearningWorker.kt` — daily `CoroutineWorker`, runs `PatternLearner`, stores results

### NotificationUtils additions
```kotlin
const val CHANNEL_INSIGHTS = "aegis_insights_channel"
// + channel creation in createNotificationChannels()
// + showInsightAlert() for non-urgent intelligence notifications
```

---

## 22 — feat: sync learned patterns and threat events to Firebase

**Files:** (modifications to existing)

- `analytics/PatternLearner.kt` — upload learned routines to `learned_patterns/{uid}/`
- `analytics/ThreatRepository.kt` — upload threat events to `threat_events/{uid}/`
- `Firebase RTDB rules` suggestion: read access for family group members only
- `data/local/dao/` — ensure DAOs support the sync queries needed

---

## Notes

- All commits are conventional commits (`feat:`, `fix:`, `docs:`)
- No pushes to remote until explicitly requested
- Each commit should compile independently (no intermediate broken state)
- The `sos/` standalone module will be merged/removed when SOS screen is built (commit 6)
