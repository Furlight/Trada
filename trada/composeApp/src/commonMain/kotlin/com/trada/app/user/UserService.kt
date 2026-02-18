package com.trada.app.user

import com.trada.app.auth.TokenStorage
import com.trada.app.auth.UserResponse
import com.trada.app.network.NetworkConfig
import com.trada.app.network.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class UserService(private val tokenStorage: TokenStorage) {

    /**
     * Fetches the connected user's profile from the protected /users/me endpoint.
     * Securely attaches the JWT access token to the Authorization header (Task 1.4 Hardening).
     * Safely checks the HTTP response status before parsing the JSON to prevent serialization crashes.
     */
    suspend fun getCurrentUser(): UserResponse {
        // 1. Retrieve the token from the secure storage (Task 1.4)
        val token = tokenStorage.getAccessToken()
            ?: throw Exception("User not connected (No access token found)")

        // 2. Execute the GET request WITH the Bearer token in the headers
        val response = httpClient.get("${NetworkConfig.BASE_URL}/users/me") {
            // This is the magic key that opens the FastAPI door! 🔑
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        // 3. Safely check if the server replied with 200 OK
        if (response.status.isSuccess()) {
            // Success! Safe to parse the JSON as a UserResponse object
            return response.body()
        } else {
            // 4. Failure (e.g., 401 Unauthorized if the token is expired/invalid)
            // We read the raw error text from FastAPI instead of crashing Ktor's serializer
            val errorBody = response.bodyAsText()
            throw Exception("Failed to load profile (Code: ${response.status.value}): $errorBody")
        }
    }
}