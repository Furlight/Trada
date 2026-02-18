package com.trada.app.auth

import com.trada.app.network.NetworkConfig
import com.trada.app.network.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.submitForm
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class AuthService {

    /**
    * Sends login credentials to the FastAPI backend.
    * Safely checks the HTTP response status before parsing the JSON.
    */
    suspend fun login(email: String, password: String): TokenResponse {

        // 1. Make the request WITHOUT calling .body() immediately
        val response = httpClient.submitForm(
            url = "${NetworkConfig.BASE_URL}/auth/login",
            formParameters = parameters {
                append("username", email)
                append("password", password)
            }
        )

        // 2. Check if the server replied with 200 OK
        if (response.status.isSuccess()) {
            // Success! Safe to parse as TokenResponse
            return response.body()
        } else {
            // 3. Failure (401 Unauthorized, 422 Error, etc.)
            // We read the raw error text from FastAPI instead of crashing
            val errorBody = response.bodyAsText()

            // Throw a clean exception that will be caught by LoginScreen
            throw Exception("Server rejected login: $errorBody")
        }
    }

    /**
     * Registers a new user.
     * Endpoint: POST /auth/register
     * Safely checks the HTTP response to avoid serialization crashes on validation errors.
     */
    suspend fun register(
        email: String,
        pass: String,
        pseudo: String,
        firstName: String = "",
        lastName: String = "",
        phone: String = "",
        country: String = "",
        walletAddress: String? = null
    ): UserResponse {

        val request = RegisterRequest(
            email = email,
            password = pass,
            pseudo = pseudo,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phone,
            country = country,
            walletAddress = walletAddress
        )

        // Make the POST request
        val response = httpClient.post("${NetworkConfig.BASE_URL}/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        // --- THE FIX IS HERE ---
        // 1. Check if FastAPI successfully created the user (200 OK)
        if (response.status.isSuccess()) {
            return response.body()
        } else {
            // 2. If FastAPI rejected the request (e.g., Email already taken, bad password)
            // We read the raw error text so the UI can display it properly
            val errorBody = response.bodyAsText()
            throw Exception("Registration failed: $errorBody")
        }
    }
}