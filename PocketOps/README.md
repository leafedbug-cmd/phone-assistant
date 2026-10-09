# PocketOps — AI Android Phone Administrator

PocketOps is a personal AI phone administrator designed for Android 16 and Samsung One UI (Galaxy S24 Ultra, SM-S928U). Rather than being a nonfunctional chat mockup, PocketOps combines **Notification Intelligence**, **Autonomous Routine Automation**, and **Supervised Screen Inspection** with an offline-first deterministic rules engine and optional Google Gemini cloud reasoning.

---

## 📱 Product Architecture

```
                    ┌──────────────────────────────────────────────┐
                    │               User Interface                 │
                    │      Jetpack Compose (Material 3 Dark)       │
                    │   Home | AI Control | Alerts | Routines      │
                    │   Insights | Activity | Settings | Onboarding│
                    └──────────────────────┬───────────────────────┘
                                           │
                    ┌──────────────────────▼───────────────────────┐
                    │             PocketOpsViewModel               │
                    └──────┬───────────────┬───────────────┬───────┘
                           │               │               │
        ┌──────────────────▼─┐   ┌─────────▼────────┐   ┌──▼──────────────────┐
        │ Notification Engine│   │Automation Engine │   │ AI Reasoning Engine │
        │ • Listener Service │   │• Deterministic   │   │• OfflineRuleProvider│
        │ • Heuristics       │   │  Scheduler       │   │• GeminiAiProvider   │
        │ • Analytics & Graph│   │• Dry-Run Sim     │   │• Untrusted Prompt   │
        │ • Digest Generator │   │• State Rollback  │   │  Sanitizer          │
        │ • Protected Apps   │   │• Conflict Matrix │   │• Risk Tiers         │
        └─────────┬──────────┘   └─────────┬────────┘   └──┬──────────────────┘
                  │                        │               │
                  └────────────────────────┼───────────────┘
                                           │
                    ┌──────────────────────▼───────────────────────┐
                    │       Security & Approval Engine             │
                    │ • Risk Tiers: Low / Medium / High            │
                    │ • Interactive Approval Checkpoints           │
                    │ • Global Emergency Stop                     │
                    │ • App Denylist & Keystore Credentials        │
                    └──────────────────────┬───────────────────────┘
                                           │
                    ┌──────────────────────▼───────────────────────┐
                    │              Android Services                │
                    │ • PocketOpsNotificationListenerService       │
                    │ • PocketOpsAccessibilityService              │
                    │ • BootAndStateReceiver                       │
                    │ • Room Database (SQLite, 5 DAOs)             │
                    │ • EncryptedSharedPreferences (Keystore)      │
                    └──────────────────────────────────────────────┘
```

---

## 🚀 Key Features

### 1. Notification Intelligence & Noise Reduction
- **Real-Time Notification Listener**: Ingests notification metadata (package, channel, app name, post timestamp, system categories).
- **Deterministic Heuristic Categorization**:
  - `Critical`: Banking, fraud alerts, 2FA/OTP passcodes, phone calls, emergency alerts (strictly protected from accidental silencing).
  - `Messages`: SMS, WhatsApp, Signal, Telegram, Discord.
  - `Work`: Slack, Microsoft Teams, Outlook, Work email, Google Meet.
  - `Shopping & Offers`: Amazon, DoorDash, Uber Eats, eBay, promo discounts.
  - `Social Media`: Instagram, TikTok, Twitter/X, Facebook, Reddit.
  - `Entertainment`: YouTube, Netflix, Spotify, Twitch.
  - `System & OS`: OS updates, Google Play, Battery notifications.
- **24-Hour Activity Visualizer**: Real-time hourly distribution bar chart.
- **Daily Notification Digest**: On-demand and scheduled summaries dividing alerts into Critical, Messages, Work, Shopping, and Social updates.

### 2. Autonomous Routine Mode
- **Deterministic Scheduling**: Local rules evaluated without needing a network connection.
- **Triggers**: Time & Day-of-week windows (e.g. Mon-Fri 8:00 AM - 5:00 PM), Charging connected/disconnected, manual triggers.
- **Actions**:
  - Activate PocketOps Work / Focus profile.
  - Interruption filter adjustment (Priority DND via `NotificationManager`).
  - Notification category suppression (e.g. filter social & shopping while working).
  - Daily summary digest delivery.
- **Conflict Detection & Safe Rollback**: Detects competing rules in identical conflict groups; snapshots previous DND state and active mode for 1-tap state restoration.
- **Dry-Run Simulator**: Evaluates routine safety, missing permissions, and projected actions before enabling.

### 3. Supervised AI Control Mode
- **Accessibility Inspection**: Safe inspection of `AccessibilityNodeInfo` hierarchy (`UiNodeSnapshot`).
- **Supervised Phone Navigation**: Interactive step-by-step assistance for system screens with explicit user authorization checkpoints.
- **Redaction Guard**: Password fields (`isPassword`) and sensitive credentials are never read, captured, or transmitted.
- **Emergency Stop**: Instantly halts all background routines and navigation operations system-wide.

### 4. Samsung One UI Integration
- **Platform Detection**: Detects Samsung Galaxy S24 Ultra (`SM-S928U`) and One UI version via system properties.
- **One UI Routine Blueprints**: Step-by-step assisted configuration for native One UI features ("Galaxy Deep Work Mode", "Galaxy Bedtime Battery & Calm", "Galaxy Commute").
- **Direct Settings Launcher**: 1-tap navigation directly into Samsung's native Modes & Routines interface.

### 5. Modular AI Reasoning Engine
- **OfflineRuleAiProvider**: Instant deterministic offline intent classification for personal phone administration queries (works 100% offline, zero tokens, zero network requests).
- **GeminiAiProvider**: Google Gemini cloud model client using OkHttp and JSON serialization with structured JSON schema enforcement.
- **Hardware Keystore Storage**: API keys are encrypted with `MasterKey.AES256_GCM` in Android Keystore.
- **Opt-In Privacy**: Cloud AI is disabled by default until the user explicitly consents.

---

## 🛠️ Build & Installation

### Requirements
- **JDK**: Java 17 LTS
- **Android SDK**: API 36 (Android 16 Target), Minimum SDK: API 28 (Android 9.0+)
- **Build System**: Gradle 9.1.0 with Configuration Cache

### Build Debug APK
```powershell
cd PocketOps
.\gradlew assembleDebug
```
The compiled APK is located at:
`PocketOps\app\build\outputs\apk\debug\app-debug.apk`

### Run Unit Tests
```powershell
.\gradlew testDebugUnitTest
```
All 17 automated tests verify:
- Daylight Saving Time and America/Chicago timezone transitions
- Schedule trigger window logic and conflict detection
- Notification classification and protected alert safeguards
- Natural language intent parsing and prompt injection protection
- Risk tier evaluation and emergency stop behavior

---

## 📱 Device Deployment (Samsung Galaxy S24 Ultra)

### Step 1: Enable USB Debugging on Galaxy S24 Ultra
1. Open **Settings** → **About phone** → **Software information**.
2. Tap **Build number** 7 times to enable Developer options.
3. Return to **Settings** → **Developer options**.
4. Enable **USB debugging**.

### Step 2: Install via ADB
Connect your Galaxy S24 Ultra to your PC via USB cable, then execute:
```powershell
adb devices
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

### Step 3: Launch and Grant Permissions
Launch **PocketOps** from your app drawer. Complete the onboarding wizard and grant:
1. **Notification Listener Access**: Allows PocketOps to ingest notification metadata.
2. **Accessibility Service**: Allows supervised navigation and screen inspection.
3. **Do Not Disturb (Notification Policy) Access**: Enables focus routine automation.

---

## 🔒 Security & Privacy Commitments
- **No Cloud Transmission without Consent**: All notification logs, statistics, and automation rules are stored locally in an on-device Room SQLite database.
- **Sensitive Field Protection**: Password inputs are marked `isPassword = true` and redacted at the accessibility level.
- **Auditable Log**: Every automated or supervised action is recorded in the searchable Activity Audit Log.
