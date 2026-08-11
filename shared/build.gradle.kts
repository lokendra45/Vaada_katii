import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    // Required for RevenueCat KMP SDK
    sourceSets.named { it.lowercase().startsWith("ios") }.configureEach {
        languageSettings {
            optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    android {
       namespace = "com.gaatho.rent.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.biometric)
            implementation(libs.androidx.work.runtime.ktx)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.materialIconsExtended)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Supabase
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.storage)
            implementation(libs.supabase.compose.auth)

            // Room & SQLite
            implementation(libs.room3.runtime)
            implementation(libs.room3.paging)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.sqlite)
            
            // Paging
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)

            // FileKit
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs.compose)

            // Ktor & Logging
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentnegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kermit)

            // Kotlinx Serialization & DateTime
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network)



            // RevenueCat
            api(libs.purchases.core)
            implementation(libs.purchases.result)
            implementation(libs.purchases.ui)

            // Orbit MVI
            implementation(libs.orbit.core)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)

            // Navigation 3 — Compose Multiplatform
            implementation(libs.jetbrains.navigation3.ui)
            implementation(libs.jetbrains.material3.adaptiveNavigation3)
            implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)

            // Sandwich — ApiResponse wrapper for Supabase / Ktor calls
            implementation(libs.sandwich)
            implementation(libs.sandwich.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room3.compiler)
    add("kspAndroid", libs.room3.paging)
    add("kspIosSimulatorArm64", libs.room3.compiler)
    add("kspIosSimulatorArm64", libs.room3.paging)
    add("kspIosArm64", libs.room3.compiler)
    add("kspIosArm64", libs.room3.paging)
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}