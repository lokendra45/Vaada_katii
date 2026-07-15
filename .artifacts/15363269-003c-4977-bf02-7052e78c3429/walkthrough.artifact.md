# Walkthrough - ErrorMessageExtractor Fixes

I have fixed the compilation errors in `ErrorMessageExtractor.kt` caused by platform-specific exceptions and changes in the Sandwich library's API.

## Changes Made

### KMP Compatibility
Removed the `SecurityException` branch from the exception mapper. `SecurityException` is a JVM-specific type and is not available in the `commonMain` source set of a Kotlin Multiplatform project.

### Sandwich Library API Updates
Updated the `extract` functions for Sandwich's `ApiResponse`. In Sandwich 2.3.0, the `Failure.Error` and `Failure.Exception` classes are no longer generic (they inherit from `Failure<Nothing>`).
- Removed `<*>` type arguments from `ApiResponse.Failure.Error` and `ApiResponse.Failure.Exception`.
- This also resolved the `Unresolved reference 'throwable'` error, as the compiler can now correctly identify the members of the `Exception` class.

## Verification Results

### Automated Tests
- Ran `:shared:assemble` - **SUCCESS**
- The project now builds correctly for all targets.
