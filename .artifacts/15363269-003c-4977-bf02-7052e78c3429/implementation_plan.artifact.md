# Implementation Plan - Update Navigation 3 to Latest CMP Setup

Update the project's Navigation 3 implementation to match the latest Compose Multiplatform setup and versions as provided in the documentation snippet.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/loke_Machine/Desktop/RentManagerApp/gradle/libs.versions.toml)
- Update versions:
    - `multiplatform-nav3-ui` from `1.1.0` to `1.1.1`.
    - `compose-multiplatform-lifecycle` to `2.10.0`.
- Update library definitions to match the provided snippet.
- Add `navigation3-browser` library.

#### [MODIFY] [settings.gradle.kts](file:///C:/Users/loke_Machine/Desktop/RentManagerApp/settings.gradle.kts)
- Add JetBrains dev repository (`https://packages.jetbrains.team/maven/p/cmp/dev`) to `dependencyResolutionManagement` to ensure `1.1.1` version of Navigation 3 is accessible.

#### [MODIFY] [shared/build.gradle.kts](file:///C:/Users/loke_Machine/Desktop/RentManagerApp/shared/build.gradle.kts)
- Sync dependency names if any changed in `libs.versions.toml`.

### Navigation Code

#### [MODIFY] [AppNavigation.kt](file:///C:/Users/loke_Machine/Desktop/RentManagerApp/shared/src/commonMain/kotlin/com/gaatho/rent/core/navigation/AppNavigation.kt)
- Verify imports and ensure compatibility with version `1.1.1`.
- Specifically check `rememberNavBackStack` and `SavedStateConfiguration` usage.

## Verification Plan

### Automated Tests
- Run `./gradlew :shared:assemble` to verify compilation.
- Run `./gradlew :androidApp:assembleDebug` to verify the Android app builds with the new dependencies.

### Manual Verification
- Deploy to Android device/emulator and verify that navigation between `PropertyListScreen` and placeholders still works.
