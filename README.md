# Vaada Katii (Rent Manager)

A modern, cross-platform property and tenant management application built for landlords. 
Built using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, targeting Android and iOS from a single shared codebase.

## Tech Stack
* **UI**: Compose Multiplatform
* **Architecture**: Orbit MVI + Clean Architecture
* **Local Database**: SQLDelight
* **Offline Sync**: Store5 + Android WorkManager
* **Backend**: Supabase (PostgreSQL, Auth, Edge Functions)
* **Dependency Injection**: Koin

## Project Structure
* `/shared` - Core business logic, KMP database, network repositories, and Compose UI.
* `/androidApp` - Android entry point and platform-specific code.
* `/iosApp` - iOS entry point and Xcode project.
* `/supabase` - Supabase edge functions and local configuration.

## Setup & Running
1. Clone the repository
2. Ensure you have Android Studio / IntelliJ IDEA installed with the Kotlin Multiplatform plugin.
3. Provide Supabase configuration keys in your local environment.
4. Run the Android app via Gradle: `./gradlew :androidApp:installDebug`
5. Run the iOS app by opening `iosApp/iosApp.xcworkspace` in Xcode.