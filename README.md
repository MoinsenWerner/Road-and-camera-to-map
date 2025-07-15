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

The initial version only contains placeholders and does not yet perform sign detection or offline map storage.

## Project structure

```
app/
  src/main/java/com/example/roadcamera/MainActivity.kt
  src/main/res/layout/activity_main.xml
  build.gradle
```

Further modules will be added for camera processing, GPS logging and map generation.
