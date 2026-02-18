import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    kotlin("plugin.serialization") version "2.1.0"
    alias(libs.plugins.buildConfig)
}

val envProperties = Properties()
val envFile = rootProject.file(".env")
if (envFile.exists()) {
    envProperties.load(FileInputStream(envFile))
}

buildConfig {
    packageName("com.trada.app")

    // 1. Retrieve values from the .env file (with safe fallbacks)
    val appEnv = envProperties.getProperty("APP_ENV") ?: "dev"
    val urlProd = envProperties.getProperty("URL_PROD") ?: "https://api.trada.com"
    val urlDev = envProperties.getProperty("URL_DEV") ?: "http://localhost:8000"

    // 2. Inject them into the generated BuildConfig class
    buildConfigField("String", "APP_ENV", "\"$appEnv\"")
    buildConfigField("String", "URL_PROD", "\"$urlProd\"")
    buildConfigField("String", "URL_DEV", "\"$urlDev\"")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    val isMac = System.getProperty("os.name").lowercase().contains("mac")
    if (isMac) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("io.ktor:ktor-client-okhttp:3.0.1")
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
            implementation("org.slf4j:slf4j-android:1.7.36")
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
            implementation("org.jetbrains.compose.material:material-icons-extended:1.6.11")
            implementation("org.jetbrains.compose.components:components-resources:1.6.11")
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
            implementation("io.ktor:ktor-client-logging:3.0.1")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(npm("@js-joda/core", "3.2.0"))
            }
        }
    }
}

android {
    namespace = "com.trada.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.trada.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    sourceSets {
        getByName("main") {
            // Prod folder : contains strict XML file
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
        }
        getByName("debug") {
            // Dev folder : contains exception 10.0.2.2
            res.srcDirs("src/androidDebug/res")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
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
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
