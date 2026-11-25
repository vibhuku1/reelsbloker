# ScrollStop – Android Shorts & Reels Blocker

> Block YouTube Shorts and Instagram Reels without blocking the rest of these apps.

---

## Overview

**ScrollStop** is a native Android application built with Kotlin and Jetpack Compose that uses the Android Accessibility Service to detect and block YouTube Shorts and Instagram Reels in real-time, while keeping all other features of these apps fully usable.

---

## Features

| Feature | Status |
|---|---|
| Block YouTube Shorts | ✅ |
| Block Instagram Reels | ✅ |
| Normal YouTube videos allowed | ✅ |
| Normal Instagram content allowed | ✅ |
| Multi-signal detection engine | ✅ |
| Safe fallback (no false positives) | ✅ |
| Local statistics | ✅ |
| Dark/Light/System theme | ✅ |
| Onboarding with permission guide | ✅ |
| No internet access required | ✅ |
| No data collection | ✅ |
| Boot reminder notification | ✅ |

---

## Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Database:** Room (local blocking event log)
- **Settings:** DataStore Preferences
- **Background:** Android AccessibilityService
- **Min SDK:** API 26 (Android 8.0)
- **Target SDK:** API 35
- **Build:** Gradle (Kotlin DSL)

---

## Project Structure

```
ScrollStop/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/scrollstop/app/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── model/
│   │   │   │   │   ├── AppSettings.kt
│   │   │   │   │   ├── BlockEvent.kt
│   │   │   │   │   └── Statistics.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/           # Room DB entities & DAO
│   │   │   │   │   ├── datastore/          # DataStore Preferences
│   │   │   │   │   └── repository/         # Single repository layer
│   │   │   │   ├── detection/
│   │   │   │   │   ├── DetectionResult.kt
│   │   │   │   │   ├── YouTubeShortsDetector.kt
│   │   │   │   │   └── InstagramReelsDetector.kt
│   │   │   │   ├── service/
│   │   │   │   │   ├── ScrollStopAccessibilityService.kt
│   │   │   │   │   ├── BootReceiver.kt
│   │   │   │   │   └── ServiceStateHolder.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/              # Colors, Typography, Theme
│   │   │   │   │   ├── navigation/         # NavHost + routes
│   │   │   │   │   └── screens/
│   │   │   │   │       ├── MainScaffold.kt
│   │   │   │   │       ├── HomeScreen.kt
│   │   │   │   │       ├── StatisticsScreen.kt
│   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │       ├── AboutScreen.kt
│   │   │   │   │       ├── OnboardingScreen.kt
│   │   │   │   │       └── BlockingActivity.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── MainViewModel.kt
│   │   │   │   └── utils/
│   │   │   │       └── AccessibilityUtils.kt
│   │   │   ├── res/
│   │   │   │   ├── values/strings.xml
│   │   │   │   ├── values/themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── accessibility_service_config.xml
│   │   │   │       ├── backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/                           # Unit tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
└── settings.gradle.kts
```

---

## How Detection Works

### Multi-Signal Detection Engine

ScrollStop uses a **confidence-scoring multi-signal approach** – never relying on a single hardcoded text string. Each detector independently evaluates:

1. **Window class name** – Known Shorts/Reels fragment class names (highest weight)
2. **Resource IDs** – Accessibility resource-id fragments specific to Shorts/Reels containers
3. **Text/content descriptions** – Visible text labels like "Shorts", "Reels" at tab/navigation level
4. **Navigation tab selection state** – The selected tab's content description

A **detection threshold of ≥ 0.5 confidence** is required before any blocking action is taken. If confidence is below threshold, the screen is left untouched (safe fallback).

### YouTube Shorts Detection Signals

| Signal | Weight | Example |
|--------|--------|---------|
| Class name | 0.60 | `ShortsPivotFragment` |
| Resource ID | 0.40 | `reel_player_container`, `shorts_shelf` |
| Visible text | 0.50 | "Shorts", "YouTube Shorts" |

### Instagram Reels Detection Signals

| Signal | Weight | Example |
|--------|--------|---------|
| Class name | 0.60 | `ClipsViewerFragment` |
| Resource ID | 0.50 | `clips_tab`, `reels_viewer_fragment` |
| Nav tab text | 0.50 | "Reels" tab selected |
| Visible text | 0.40 | Exact "reels" label match only |

### Safe Fallbacks

- If YouTube/Instagram changes their UI and no signals fire → **no block occurs**
- If confidence is below threshold → **no block occurs**
- Cooldown of 3 seconds prevents repeated triggers on the same screen

---

## Building the APK

### Prerequisites

- Android Studio Ladybug (2024.2.x) or later
- JDK 17+
- Android SDK with API 35
- Android device or emulator running Android 8.0+ (API 26+)

### Steps

```bash
# 1. Open the project in Android Studio
# File → Open → select the ScrollStop/ folder

# 2. Let Gradle sync complete

# 3. Build debug APK
./gradlew assembleDebug

# 4. Build release APK (unsigned – for signing see below)
./gradlew assembleRelease

# APK output location:
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release-unsigned.apk
```

### Signing the Release APK

```bash
# Generate a keystore (do this once)
keytool -genkey -v -keystore scrollstop.jks -keyalg RSA \
  -keysize 2048 -validity 10000 -alias scrollstop

# Sign with apksigner (from Android SDK build-tools)
apksigner sign \
  --ks scrollstop.jks \
  --ks-alias scrollstop \
  --out app/build/outputs/apk/release/ScrollStop-v1.0.0.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## Installing the APK on Your Android Phone

### Method 1: ADB (USB)

```bash
# 1. Enable Developer Options on your phone:
#    Settings → About Phone → tap "Build Number" 7 times
#
# 2. Enable USB Debugging:
#    Settings → Developer Options → USB Debugging → ON
#
# 3. Connect phone via USB and run:
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Method 2: File Transfer (Sideload)

1. Build the APK (`./gradlew assembleDebug`)
2. Copy `app-debug.apk` to your phone via USB, email, or cloud storage
3. On your phone: **Settings → Security → Install Unknown Apps** → allow your file manager
4. Open the APK file and tap **Install**

### Method 3: Android Studio (Wireless)

1. Connect your phone to Android Studio (Pair Devices over Wi-Fi or USB)
2. Click **Run ▶** to build and install in one step

---

## First Launch Setup

1. Open ScrollStop
2. Follow the onboarding steps
3. Tap **"Open Accessibility Settings"**
4. Find **"ScrollStop – Shorts & Reels Blocker"** in the list
5. Toggle it **ON** and confirm the dialog
6. Return to ScrollStop – you're protected!

> **Important:** The Accessibility Service must be manually re-enabled after every device restart. This is an Android security requirement that cannot be bypassed. ScrollStop will show a boot reminder notification.

---

## Running Tests

```bash
# Unit tests (JVM, no device required)
./gradlew test

# Instrumented tests (requires connected device)
./gradlew connectedAndroidTest
```

### Test Coverage

| Test | Type | Coverage |
|------|------|----------|
| `DetectionEngineTest` | Unit | YouTube/Instagram detector logic, false-positive prevention, cross-checks |
| `ModelTest` | Unit | Data model defaults, computed properties, timestamp accuracy |

### Scenarios Requiring Real Device Testing

The following must be tested manually on a physical device with YouTube and Instagram installed:

**YouTube:**
- [ ] Open normal video → should NOT be blocked
- [ ] Search for video → should NOT be blocked
- [ ] Open Shorts via home tab → should be blocked
- [ ] Navigate Home → Shorts → should be blocked
- [ ] Open channel page → should NOT be blocked
- [ ] Play normal video after leaving Shorts → should NOT be blocked

**Instagram:**
- [ ] Open Home feed → should NOT be blocked
- [ ] Open Stories → should NOT be blocked
- [ ] Open Direct Messages → should NOT be blocked
- [ ] Open normal video post → should NOT be blocked
- [ ] Open Reels tab → should be blocked
- [ ] Open Reel from Explore → should be blocked
- [ ] Return to normal feed → should NOT be blocked

**App behaviour:**
- [ ] Enable/disable protection via toggle
- [ ] Restart phone → service needs re-enabling (boot reminder shown)
- [ ] Disable Accessibility permission → dashboard shows warning
- [ ] Change theme (Dark/Light/System)
- [ ] Navigate all 4 screens

---

## Privacy Commitments

ScrollStop is designed with privacy as a core requirement:

- ❌ No account or login
- ❌ No internet permission used
- ❌ No personal data collected
- ❌ No messages or passwords accessed
- ❌ No screen recording
- ❌ No analytics or advertising
- ✅ All data stays on your device
- ✅ Accessibility Service reads only UI structure (not content)
- ✅ Open about what it does in the accessibility service description

---

## Technical Limitations

| Limitation | Explanation |
|------------|-------------|
| Service requires manual re-enable after restart | Android security requirement for all accessibility services |
| Detection depends on YouTube/Instagram UI | If apps significantly update their UI, detection signals may need updating |
| Cannot guarantee 100% detection | Obfuscated resource IDs or heavily modified UIs may evade detection – safe fallback applies |
| Battery usage | Accessibility services have a small but non-zero battery overhead; optimised with 100ms debounce |

---

## License

ScrollStop is provided as-is for personal use. See individual library licenses for dependencies.
