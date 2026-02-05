package com.trada.app.auth

import com.trada.app.network.NetworkConfig
import com.trada.app.network.httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class AuthService {
    /**
     * Sends login credentials to the FastAPI backend.
     * Uses formUrlEncoded as required by OAuth2PasswordRequestForm in FastAPI.
     */
    suspend fun login(email: String, password: String): TokenResponse {
        return httpClient.submitForm(
            url = "${NetworkConfig.BASE_URL}/auth/login",
            formParameters = parameters {
                append("username", email) // FastAPI expects 'username' field
                append("password", password)
            }
        ) {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
        }.body()
    }

    /**
     * Sends a registration request with a JSON body.
     * Unlike login, registration usually expects a standard JSON payload.
     */
    suspend fun register(email: String, password: String): UserResponse {
        return httpClient.post("${NetworkConfig.BASE_URL}/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(UserCreateRequest(email, password)) // We need this small DTO
        }.body()
    }

    @Serializable
    data class UserCreateRequest(val email: String, val password: String)
}