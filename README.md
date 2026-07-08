# Bethesda Biblical Institute — Android App

A native Android wrapper around **https://bethesdabiblicalinstitute.com/**, built with Kotlin.
Since the site is a full PHP + MySQL application (admissions forms, file uploads, login,
donations), the standard and most reliable way to turn it into an app is a WebView-based
client — the alternative (rebuilding the whole PHP/MySQL backend as native Kotlin) would mean
re-implementing the entire site and database logic twice, for no real benefit to your users.

## What this app does
- Loads your live site in a full-screen WebView with a navy/gold theme matching your brand
- Branded splash screen + adaptive launcher icon (gold cross on navy)
- Keeps users logged in (cookies persist between app opens) — Student Login works normally
- Handles the **admissions/affiliation upload forms** (photo, ID, certificate uploads) via the
  native Android file/camera picker
- Handles **certificate/PDF downloads** through the Android Download Manager
- Routes `tel:`, `mailto:`, and WhatsApp (`wa.me`) links to the appropriate native apps instead
  of trying to load them in-page
- Pull-to-refresh, a progress bar, and a friendly "You're offline" screen with Retry when
  there's no connection
- Back button navigates WebView history before exiting the app

## Requirements to build
- [Android Studio](https://developer.android.com/studio) (Koala or newer recommended)
- JDK 17 (bundled with recent Android Studio)
- Internet access to download Gradle + dependencies the first time you open the project

## How to build the APK

1. Unzip this project and open the folder in Android Studio (`File > Open`).
2. Let Gradle sync finish (first run downloads the Android Gradle Plugin + dependencies —
   this is the one thing that couldn't be done in the sandbox that generated this project,
   since it has no access to Google's Maven repositories).
3. Build the APK:
   - **Debug APK (fastest, for testing):** `Build > Build App Bundle(s) / APK(s) > Build APK(s)`
     → output lands in `app/build/outputs/apk/debug/app-debug.apk`
   - **Or from a terminal:** `./gradlew assembleDebug`
4. To install straight to a connected phone: `Run ▶` in Android Studio, or
   `./gradlew installDebug`.

### Release build (for the Play Store)
1. `Build > Generate Signed Bundle / APK`, create/select a keystore, choose `APK` or `AAB`.
2. Or via terminal after configuring signing in `app/build.gradle`:
   `./gradlew assembleRelease`

## Customizing

| Want to change...            | Edit this file |
|---|---|
| The site URL                 | `app/src/main/res/values/strings.xml` → `base_url` |
| App name shown under icon    | `app/src/main/res/values/strings.xml` → `app_name` |
| Theme colors (navy/gold)     | `app/src/main/res/values/colors.xml` |
| Launcher icon                | `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml` (vector) or replace the PNGs in the `mipmap-*` folders with your own artwork |
| Splash screen                | `app/src/main/res/values/themes.xml` → `Theme.Bethesda.Splash` |
| Which external domains open outside the app | `MainActivity.kt` → `shouldOverrideUrlLoading` |

## Notes
- `applicationId` is set to `com.bethesda.institute` — change this in `app/build.gradle` before
  publishing if you'd prefer a different package name (it can't be changed after you publish to
  the Play Store).
- Network security config restricts the app to HTTPS traffic only, matching your live site.
- Minimum supported Android version: 7.0 (API 24). Target/compile: Android 14 (API 34).
