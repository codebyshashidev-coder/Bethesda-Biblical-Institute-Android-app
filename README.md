# Bethesda Biblical Institute — Android App

A native Android app for **bethesdabiblicalinstitute.com**, built with Kotlin and designed to
match the Stitch design system (navy `#00236F` / gold `#D97706`, Playfair Display + Work Sans).

## ⚠️ About the database credentials

Your `DB_HOST` / `DB_USER` / `DB_PASS` / `DB_NAME` values are **not included anywhere in this
app**, and they never should be. Here's why, and what to do instead:

- An APK is just a zip file — anyone can decompile it in minutes with free tools. Any credential
  baked into the app (a DB password, an API secret key, etc.) becomes public the moment someone
  downloads your app.
- A mobile app should **never** connect directly to a MySQL database. It should only ever talk to
  your web server over HTTPS, and your web server (which already has the credentials, safely, in
  `includes/db.php`) does the actual database work.
- **What to do with those credentials:** make sure they're set correctly in
  `includes/db.php` on your actual hosting server (they look like real production values, e.g.
  from a cPanel-style host) — that's the one and only place they belong. Don't paste them into
  chat, code, or the app again if you can avoid it; consider rotating (changing) that DB password
  from your hosting control panel as good practice any time it's been shared outside the server.

## Architecture

This app follows that safe pattern:

- **Home, Courses, Admissions tabs** — native Kotlin/Material UI, styled to the Stitch design
- **News** — fetched live from a small new JSON endpoint, `api/news.php`, which you upload to
  your site. It reuses your existing `includes/db.php` connection — it does not contain or
  duplicate your credentials.
- **Admissions "Apply Online", program detail pages, Profile/login** — open your site's existing,
  already-working PHP pages in an in-app WebView. This reuses your real (and already tested) file
  upload handling, form validation, and session-based login exactly as-is — safer and far less
  work than rebuilding those flows natively.

## Setting up `api/news.php`

1. Copy `api/news.php` (included in this project) to your server at
   `/bethesdabiblicalinstitute/api/news.php` (create the `api` folder if it doesn't exist).
2. That's it — it automatically uses your site's existing `includes/db.php`. Test it by visiting
   `https://bethesdabiblicalinstitute.com/api/news.php` in a browser; you should see JSON output.

## What's in the app

- Bottom navigation: **Home · Courses · Admissions · Profile**
- Splash screen + adaptive launcher icon (gold cross on navy)
- Home: hero with Apply Now / Learn More, "Why Study With Us", live Latest News list
- Courses: native cards for each program level (Certificate → Doctoral), tapping opens the full
  page on your site
- Admissions: Apply Online Now, Admission Process, Affiliation & Accreditation
- Profile: your site's real student login (cookies/session persist between app opens)
- File/camera picker wired up for admissions upload forms (photo, ID, certificates)
- Certificate/PDF downloads via Android's Download Manager
- Offline screen with Retry, pull-to-refresh, progress bar

## Requirements to build

- [Android Studio](https://developer.android.com/studio) (Koala or newer)
- JDK 17 (bundled with recent Android Studio)

## How to build the APK

1. Unzip this project and open it in Android Studio, or use the included GitHub Actions
   workflow (`.github/workflows/build-apk.yml`) to build it in the cloud — push to your repo and
   download the APK from the Actions tab's build artifacts.
2. Locally: `Build > Build App Bundle(s) / APK(s) > Build APK(s)`, or `./gradlew assembleDebug`
   — output lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Customizing

| Want to change...         | Edit this file |
|---|---|
| Site URL / API URL        | `app/src/main/res/values/strings.xml` |
| Program list (Courses tab)| `app/src/main/java/com/bethesda/institute/model/Program.kt` |
| Theme colors               | `app/src/main/res/values/colors.xml` |
| Home screen layout        | `app/src/main/res/layout/fragment_home.xml` |
| Launcher icon              | `mipmap-anydpi-v26/ic_launcher.xml` + PNGs in `mipmap-*` |

## Notes

- `applicationId` is `com.bethesda.institute` — change it in `app/build.gradle` before publishing
  if you want something else (can't be changed after publishing to the Play Store).
- Minimum Android version: 7.0 (API 24). Target/compile: Android 14 (API 34).
