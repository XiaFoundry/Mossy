# Mossy · Savings Companion 🌱

**English** · [简体中文](README.zh-CN.md) · [繁體中文](README.zh-TW.md) · [日本語](README.ja.md) · [한국어](README.ko.md)

**A simple, clean, privacy-first savings tool for Android.**

Mossy helps you set savings goals, log every deposit, and watch your targets come to life through an intuitive ring chart.

No account. No bank card. No server. You just log what you save — the pace is entirely up to you.



## ✨ Features

- **🎯 Visual goals** — See your overall savings progress in a ring chart, so every bit of effort leaves a trace.
- **🪙 Flexible logging** — Save toward a single goal or split one deposit across several; backfill past entries anytime.
- **📊 Clear history** — Browse savings entries by batch and trace every single record.
- **🏆 Lightweight achievements** — Milestones and streaks add a bit of fun to the long haul.
- **🎨 Personal themes** — Light/dark mode, multiple color palettes, plus dynamic color on Android 12+.
- **💾 Your data, your call** — Stored locally, with JSON backup and restore.
- **⏰ Optional reminders** — Local notifications remind you to log; no server involved.



## 🔒 Privacy first

Mossy follows one simple rule: **your money is yours, and so is your data.**

- No account or sign-in
- No bank accounts linked, no funds handled
- Data stays on your device, no remote server
- Export and restore your data whenever you want

Mossy is a savings tracking tool — not a bank, a payment service, or a financial platform.



## 🚀 Getting started

### Requirements

- Android 8.0 (API 26) or higher



### Build from source

Install [Android Studio](https://developer.android.com/studio) and set up JDK 17 and the Android SDK.

```
git clone https://github.com/XiaFoundry/Mossy.git
cd Mossy
./gradlew :app:assembleDebug
```

The generated debug APK is located at:

```
app/build/outputs/apk/debug/app-debug.apk
```

Run unit tests:

```
./gradlew :app:testDebugUnitTest
```

You can also open the project in Android Studio, wait for Gradle sync to finish, and run `app`.



## 🛠️ Tech stack

Kotlin · Jetpack Compose · Material 3 · Room · DataStore · Coroutines · StateFlow

Layered architecture, amounts stored as integer cents, and unit tests covering the core business logic.



## 📄 License

This project is currently for learning and personal use only.

If Mossy helps you, a Star ⭐ is welcome — and so are suggestions and feedback.
