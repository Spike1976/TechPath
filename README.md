# TechPath Android

**Mission:** Learn how technology works, then build your own.

TechPath is a native Android-first technical learning system. It is project-driven rather than course-driven: the learner chooses a goal, the app assesses demonstrated knowledge, maps prerequisites, teaches each missing concept, requires a practical knowledge check, and always shows what comes next and why.

## Current beta scope

Implemented in source:

- Native Kotlin + Jetpack Compose Android structure
- Local-first curriculum and progress storage
- Goal selection and project paths
- Starting-point diagnostic that does not reveal lesson answers during the test
- Adaptive next-concept selection based on prerequisites and demonstrated mastery
- 14 technical categories
- 144 curated concepts
- 8 end-to-end project learning paths
- Search across concepts and real-world uses
- Per-concept plain-language explanation, why it matters, prerequisites, five-step learning sequence, hands-on task, real-world uses, knowledge check, next steps, and safety/authorization notes where appropriate
- Persistent started/mastered progress
- Per-category and overall mastery dashboards
- Build/project readiness percentages
- Legal/safe device-analysis boundary content for owner-authorized hardware
- Lab hub for networking, BLE, USB/serial, ESP32 integration, and device-analysis learning

## Knowledge categories

1. Programming Foundations
2. Android Development
3. Electronics
4. ESP32
5. Raspberry Pi
6. Networking
7. Device Interfaces & Protocols
8. Data & Visualization
9. Weather Technology
10. Drones & Flight Systems
11. RC Cars & Aircraft
12. Automotive & Dash Cameras
13. Flipper & Hardware Tools
14. Safe Device Analysis

## Project paths

- ESP32 Weather Station
- Better Dash-Camera Dashboard
- BLE Sensor Interface
- RC Telemetry & Control Interface
- Drone Ground-Station Viewer
- Raspberry Pi Sensor Gateway
- Flipper Hardware Learning Lab
- Native Android Technical Dashboard

## Learning rule

Completion is not awarded merely because a screen was opened. A concept is marked mastered only after the learner answers the practical knowledge check and explicitly confirms they can explain and use the concept.

The starting-point diagnostic is separate from lessons. It does not show explanatory content or “learn this first” hints while the assessment is active.

## Content quality rules

The curriculum is validated for:

- unique concept IDs
- valid category references
- valid prerequisite references
- valid next-step references
- valid project-path references
- minimum explanation depth
- minimum five learning steps per concept
- hands-on exercise presence
- real-world-use presence
- knowledge-check presence

`validate_catalog.py` performs these checks.

## Architecture

- `model/` — curriculum, concept, project, and progress models
- `engine/` — learning-path and recommendation logic
- `data/` — local curriculum loader and persistent progress
- `ui/` — Compose application screens
- `assets/catalog/catalog.part*.b64` — compressed offline curriculum knowledge graph

The curriculum is bundled in the APK. `CatalogRepository` joins the asset segments, Base64-decodes them, decompresses the GZIP payload, and loads the JSON knowledge graph locally. No server is required to browse or learn from the initial curriculum.

## Current unfinished beta items

These are deliberately not faked:

- Live BLE service/characteristic explorer
- USB serial terminal
- On-device code execution/compiler sandbox
- Live phone-sensor oscilloscope
- Camera-assisted component identification
- Rich animated protocol/circuit visualizations
- Export/import of learner profile

The learning paths and content needed to teach these features already exist. Their live hardware implementation should be added as real Android integrations rather than placeholder buttons.

## Build tooling

Project configuration targets current 2026 Android tooling:

- Android Gradle Plugin 9.4.0
- Gradle 9.6 expected
- Kotlin 2.4.20
- Compose BOM 2026.09.00
- compileSdk / targetSdk 37
- minSdk 26

This workspace did not contain an Android SDK or Gradle distribution, so the full APK build could not be executed locally in this environment. The pure Kotlin learning engine compiled successfully and the original 144-concept curriculum passed integrity validation before upload.
