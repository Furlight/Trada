package com.trada.app.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object NetworkConfig {
    /**
     * Android emulator uses 10.0.2.2 to reach the host's localhost.
     * Ensure your FastAPI server is running on port 8000.
     */
    const val BASE_URL = "http://10.0.2.2:8000"
}

/**
 * Global HTTP client configured for JSON serialization and network logging.
 */
val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true // Prevents crashes if API adds new fields
            prettyPrint = true
            isLenient = true
        })
    }

    install(Logging) {
        level = LogLevel.ALL
        logger = Logger.DEFAULT
    }
}