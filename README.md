# AppGuard - Android App Blocker

An Android app that requires you to confirm multiple times before opening a protected app.

## How to Build

### Option 1: Android Studio (Recommended)

1. Open **Android Studio**
2. Click **File → Open** and select the `AppGuard` folder
3. Wait for Gradle sync to complete
4. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Command Line

```bash
cd AppGuard
./gradlew assembleDebug
```

APK location: `app/build/outputs/apk/debug/app-debug.apk`

## How to Use

1. Install the APK on your Android device (API 24+)
2. Open AppGuard
3. Tap "Target App" to select an app you want to protect
4. Set the number of confirmations required (1-50, default 10)
5. Toggle "Enable Protection" ON
6. Grant Overlay and Accessibility permissions when prompted
7. Now when you try to open the protected app, you'll need to confirm multiple times

## Requirements

- Android 7.0 (API 24) or higher
- Overlay permission (to show confirmation screen)
- Accessibility permission (to detect app launches)

## Permissions

- `SYSTEM_ALERT_WINDOW` - Shows confirmation overlay
- `BIND_ACCESSIBILITY_SERVICE` - Detects when protected app is launched
- `FOREGROUND_SERVICE` - Keeps protection active
- `QUERY_ALL_PACKAGES` - Lists installed apps for selection