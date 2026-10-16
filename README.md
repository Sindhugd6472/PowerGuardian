# PowerGuardian

AI-powered Android app for adaptive battery optimization using usage patterns and background services.

## Features
- Battery tracking
- Wake lock control
- App usage classification
- Jetpack Compose + WorkManager
- Android 14 compatible

## APK
[Download APK](https://drive.google.com/file/d/1SekFGuF6w4KmydWXVSKKNPHvZWl6K_j7/view?usp=drive_link)

## License
MIT

## Build and validation

Use JDK 17, Android SDK 34 and the Gradle wrapper: `./gradlew testDebugUnitTest assembleDebug`. Local SDK paths belong in untracked local.properties. Gradle caches, IDE state and generated app/build output are excluded from source control.

The Android 14 foreground service declares its required permissions and special-use description. WorkManager retains its own service declaration. Activity teardown unregisters its receivers and releases its wake lock; repeated acquisition reuses an already-held lock. Usage-event percentages match the logger's zero-padded date/hour and ignore continuation lines. These are event-log heuristics, not measured battery consumption or a trained AI model. AIPredictor is a random demo stub. Device validation, notification permission behavior and Android build verification are still required.
