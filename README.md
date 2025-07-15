# Road and Camera Map

This project aims to build an Android app that uses the phone's camera and GPS sensor to create and improve road maps. The app will:

- Continuously record the device's GPS position to trace road geometry.
- Use the live camera feed to detect speed‑limit signs and capture speed limits for each road segment.
- Fetch road names from an open map provider (e.g. OpenStreetMap) to associate them with recorded segments.
- Store collected map data locally so that previously visited roads can be displayed and updated when driving them again.
- Show the current speed and the known speed limit for the active road while driving.

## Getting started

The code base uses Kotlin and requires Android Studio Flamingo or newer. To build the application locally:

1. Clone the repository and open it in Android Studio.
2. Let Gradle download the dependencies.
3. Deploy the `app` module to an Android device running Android 8.0 or newer.

The project currently contains a very small prototype. Map data is stored locally and a background worker is prepared to upload/download the data so that it can be merged with recordings from other devices.

You can control this synchronisation and other behaviour in the app settings:

- Turn map data synchronisation on or off.
- Limit synchronisation to Wi‑Fi networks only.
- Choose between dark mode, light mode or follow the system setting.
- Decide whether new speed limits are detected with the camera or fetched online (in that case the limit is marked with an orange dot in the UI).

## Project structure

```
app/
  src/main/java/com/example/roadcamera/MainActivity.kt
  src/main/java/com/example/roadcamera/SettingsActivity.kt
  src/main/java/com/example/roadcamera/MapSyncManager.kt
  src/main/res/layout/activity_main.xml
  src/main/res/xml/preferences.xml
  build.gradle
```

Further modules will be added for camera processing, GPS logging and map generation.
