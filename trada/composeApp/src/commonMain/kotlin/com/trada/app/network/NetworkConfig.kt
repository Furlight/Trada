package com.trada.app.network

import com.trada.app.BuildConfig
import com.trada.app.getPlatform // This imports the default KMP platform detector
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Centralized network configuration.
 * It uses the default KMP 'getPlatform()' to detect if it's running on Android
 * and dynamically swaps 'localhost' with '10.0.2.2' without needing separate files.
 */
object NetworkConfig {

    val BASE_URL: String by lazy {
        val appEnv = BuildConfig.APP_ENV.lowercase()

        // 1. Production Mode
        if (appEnv == "prod") {
            return@lazy BuildConfig.URL_PROD
        }

        // 2. Development Mode
        val devUrl = BuildConfig.URL_DEV
        val platformName = getPlatform().name.lowercase()

        // Determine the correct localhost IP based on the platform name
        val targetLocalhost = if (platformName.contains("android")) {
            "10.0.2.2" // Android emulator magic IP
        } else {
            "localhost" // iOS simulator and Web browser
        }

        // Replace the host in the DEV url
        devUrl
            .replace("localhost", targetLocalhost)
            .replace("127.0.0.1", targetLocalhost)
    }
}

/**
 * Global HTTP client configured for JSON serialization and network logging.
 */
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Prevents crashes if the API adds new fields
            prettyPrint = true
            isLenient = true
        })
    }

    install(Logging) {
        level = LogLevel.ALL
        logger = Logger.DEFAULT
    }
}