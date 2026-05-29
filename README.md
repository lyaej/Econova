# Econova

A mobile application developed as a proposed solution by 1st year IT students at the Polytechnic University of the Philippines for their research titled "Development of a Digital Carbon Footprint Tracker for Students Using Mobile Technology."

---

## Overview

Econova allows users to calculate and track the carbon footprint of their digital lifestyle. Users select the digital devices and services they use daily, and the app computes the estimated CO2 emissions per year. Results can be exported as PNG directly to the phone gallery.

---

## Features

- One-time name input saved locally, no login required
- Digital carbon footprint calculator with various products and services
- Preset consumption profiles (Average, Intensive, Video Streaming, Home Office)
- Live bar chart, and pie chart breakdown per selected item
- Export result as PNG to phone gallery
- Saves history records for each date, with filtering by day, week, month, and year
- Clear data option that resets the app to the name input screen
- About Us page with researcher profiles and social links

---

## App Flow

|   Screen   | Description                                                                                 |
|:----------:|:--------------------------------------------------------------------------------------------|
|   Splash   | App logo displayed on launch for 2 seconds                                                  |
| Onboarding | One-time name input, stored in SharedPreferences                                            |
|    Main    | Carbon footprint calculator with presets, product selector, result card, and export buttons |
|  History   | Saves the track logs of the user                                                            |
|   About    | Research description and researcher profiles                                                |

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/dcf/tracker/
│   ├── data/
│   │   └── Prefs.kt
│   ├── model/
│   │   └── DailyLog.kt
│   └── ui/
│       ├── AboutActivity.kt
│       ├── ChartUtils.kt
│       ├── HistoryActivity.kt
│       ├── MainActivity.kt
│       └── OnboardingActivity.kt
│       └── SplashActivity.kt
└── res/
    ├── drawable/
    │   ├── logo.png
    │   ├── ic_person_placeholder.xml
    │   ├── circle_bg.xml
    │   ├── spinner_bg.xml
    │   ├── researcher1.jpg
    │   ├── researcher2.jpg
    │   └── researcher3.jpg
    ├── layout/
    │   ├── activity_splash.xml
    │   ├── activity_history.xml
    │   ├── activity_onboarding.xml
    │   ├── activity_main.xml
    │   ├── activity_about.xml
    │   ├── item_researcher.xml
    │   ├── item_log_entry.xml
    │   ├── item_hours_row.xml
    │   └── item_breakdown_row.xml
    └── values/
        ├── arrays.xml
        ├── colors.xml
        ├── strings.xml
        └── themes.xml
```

---

## Clearing Saved Data

Option 1: Tap the "Clear Data" button inside the app
Option 2: Go to Settings > Apps > econova > Storage > Clear Data

---

## Researchers

- Catague, Elljah Aneeza
- Custodio, Hannah Faith
- Lugtu, Lyka Mae

© Polytechnic University of the Philippines
Bachelor of Science in Information Technology, 1st Year

---

## Built With

- Kotlin
- Android SDK
- Material Components for Android
- AndroidX WebKit
