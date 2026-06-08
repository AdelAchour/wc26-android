# ⚽ WC26 Threads — Android Client

**WC26 Threads** is a native Android social companion application built specifically for the **FIFA World Cup 2026**. Designed with a focused, topic-centric product strategy, it serves as a community space where fans can participate in dedicated match threads—free from follower graphs, noise, or algorithms. 

Every single match of the tournament gets its own real-time discussion board, allowing fans to share their hot takes, celebrate goals, and discuss tactics.

- **Google Play Store:** [play store](https://play.google.com/store/apps/details?id=com.adel.wc26")
- **Live Web Landing Page & Demo:** [wc26.adelash.dev](https://wc26.adelash.dev/)
- **Live API Base URL:** [wc26.adelash.dev/api](https://wc26.adelash.dev/api/)
- **Backend Repository:** [wc26-backend](https://github.com/AdelAchour/wc26-backend)

---

## Project Overview & Tradeoffs

Unlike standard social apps, WC26 Threads is built on a **deliberately scoped feature set** optimized for a tournament-long fan experience:
- **Zero Algorithmic Bias**: Content is chronologically grouped by match. Users don't need to follow anyone to engage; they just find their match and join the chat.
- **Production-Ready Engineering**: Designed with the exact patterns you would use in a highly scaled corporate app—such as idempotent API designs, denormalized counters, and database-driven app-blocking states.
- **Architected for Recruiters & Developers**: Serves as a portfolio highlight showing how modern Jetpack Compose practices and clean separation of concerns can be engineered end-to-end.

---

## The Tech Stack

The application is engineered strictly with tools and patterns that represent **Google's Modern Android Development (MAD)** standards.

| Component | Technology | Rationale |
| :--- | :--- | :--- |
| **Language** | Kotlin | Standard JVM-first development with Coroutines and Serialization. |
| **UI Framework** | Jetpack Compose | Declarative UI leveraging the **Material 3** guidelines. |
| **DI** | Dagger Hilt | Compile-time dependency injection ensuring testability and modularity. |
| **Async & Flow** | Kotlin Coroutines & Flow | Asynchronous operations and reactive data propagation. |
| **Navigation** | Navigation Compose (2.8+) | **Type-safe navigation** with Kotlinx Serialization. No string route parsing. |
| **Networking** | Retrofit + OkHttp | REST API consumption with interceptors. |
| **JSON Parser** | Kotlinx Serialization | Kotlin-first JSON serialization and deserialization. |
| **Pagination** | Paging 3 (Compose) | Jetpack Paging for smooth cursor-based pagination from the server. |
| **Persistence** | Preferences DataStore | Coroutine-friendly local key-value caching (tokens, IDs, roles). |

---

## Architecture & Clean Design

The codebase strictly adheres to **Clean Architecture** principles and the **MVVM (Model-View-ViewModel)** design pattern. It is packaged **feature-by-feature** to isolate business domains, improve build speed, and prevent bloated modules.

### Directory Structure Example (`feature/matches`)
```
matches/
├── data/           # Repository implementations, DTOs, mappers, remote data sources
├── di/             # Hilt modules injecting repository and network bindings
├── domain/         # Pure Kotlin use-cases, domain models, and repo interfaces
└── ui/             # Compose Screens, ViewModels, and UI state representations
```

---

## Architecture Highlights & Best Practices

Here are some key engineering decisions implemented in this repository that follow production-level guidelines:

### 1. Type-Safe Navigation
Following Navigation Compose 2.8+, this application eliminates error-prone string route paths. Routes are declared as `@Serializable` data classes or objects:
```kotlin
@Serializable
data class MatchDetail(val matchId: Long)
```
Screen arguments are retrieved compile-checked using `toRoute<Destinations.MatchDetail>()`, preventing runtime crashes due to missing parameters or typos.

### 2. Custom OkHttp Interceptor for Session & App Control
A central `AuthInterceptor` intercepts all outgoing requests and incoming responses to orchestrate core app logic:
- **Bearer Token Attachment**: Automatically injects credentials from `TokenStore`.
- **Version Headers**: Attaches application version for API compatibility verification.
- **Session Eviction (401)**: Intercepts unauthorized tokens, wipes the local `TokenStore` reactively, and triggers a seamless redirect to the login screen.
- **Force Update (426)** & **Maintenance Mode (503)**: Buffers network streams using okio to parse blocking states and updates `AppStatusManager` to immediately present a blocker screen without disrupting UI execution.

### 3. Cursor-Based Pagination with Paging 3
Feeds are paginated using Jetpack Paging 3. The `PostPagingSource` handles server-side cursor strings (base64 tokens) to dynamically fetch next pages. It supports a generic fetch lambda, meaning the same pagination logic is reused across:
* The Global Feed
* Individual Match Detail Threads
* User Profile Feeds

### 4. Custom Design System & Rich Aesthetics
The app uses a curated, World-Cup themed dark/light color palette built with Material 3 tokens. Standard system defaults were replaced with highly polished, custom-made UI elements:
* **`LiveBadge`**: An animated pulsator indicating active live matches.
* **`Skeleton`**: Custom shimmer-loader placeholders for feeds, preventing jarring layout shifts during data fetches.
* **`TeamFlag`**: A dynamic drawable resource locator (`getIdentifier`) that binds team codes to high-resolution flag vectors.
* **`WC26Avatar`**: A custom themed selection of presets, showing the selected avatar's label dynamically.

### 5. Lifecycle-Aware Flow Observation
To avoid wasted resources (CPU and battery) when the app is in the background, all state flows are observed within Compose using `collectAsStateWithLifecycle()` rather than the standard `collectAsState()`. This guarantees the flow collection pauses when the activity is stopped.

---

## Setup & Local Build Instructions

### Prerequisites
* JDK 17+
* Android Studio Ladybug+
* Android SDK 36 (targetSdk)

### Building the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/AdelAchour/wc26-android.git
   ```
2. Open the project in Android Studio.
3. Make sure you set the `USE_LOCALHOST` to either `true` (hits local backend loopback) or `false` (hits live production URL).
4. Run the Gradle build:
   ```bash
   ./gradlew assembleDebug
   ```

### Configuration
In `app/build.gradle.kts`, base URLs are injected based on the build types:
* **Debug**: `http://10.0.2.2:8080/` (mapped to the Android Emulator loopback pointing to a local running Ktor instance).
* **Release**: `https://wc26.adelash.dev/api/` (pointing to the live backend server).

Note : To apply this logic you need to replace `.baseUrl(ApiConstants.BASE_URL)` with `.baseUrl(BuildConfig.BASE_URL)`

---

Disclaimer: This project is an independent open-source fan portfolio app and is not affiliated with, authorized, or endorsed by FIFA or any official World Cup organization.*
