import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val version = "1.0.4"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.oscquery)
            implementation(libs.osc)

            implementation(libs.napier)

            implementation(libs.spotify)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)

            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.kfswatch)

            implementation(libs.jmdns)

            implementation(kotlin("reflect"))
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "gay.lilyy.lilypad"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "gay.lilyy.lilypad"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = version.replace(".", "").toInt()
        versionName = version

        manifestPlaceholders["redirectHostName"] = "gay.lilyy.lilypad"
        manifestPlaceholders["redirectSchemeName"] = "lilypad"
    }
    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        val resourcesRoot = project.file("src/commonMain/resources")
        mainClass = "gay.lilyy.lilypad.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "gay.lilyy.lilypad"
            packageVersion = version

            macOS {
                iconFile.set(resourcesRoot.resolve("icons/icon.icns"))
            }
            windows {
                iconFile.set(resourcesRoot.resolve("icons/icon.ico"))
            }
            linux {
                iconFile.set(resourcesRoot.resolve("icons/icon.png"))
            }
        }
    }
}