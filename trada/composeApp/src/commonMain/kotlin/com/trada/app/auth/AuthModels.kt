package com.trada.app.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Data class representing the successful login response from FastAPI.
 */
@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String,

    // We make it nullable (?) and default to null (= null)
    // so the app doesn't crash if the backend forgets to send it.
    val refresh_token: String? = null
)

/**
 * Data class representing user information returned by the backend.
 * Uses @SerialName to cleanly map Python snake_case to Kotlin camelCase.
 */
@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val pseudo: String? = "Unknown",
    val is_active: Boolean = true,
    val is_verified: Boolean = false,

    // --- KYC / Identity Fields ---
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null,
    val country: String? = null,

    // --- Web3 ---
    @SerialName("wallet_address") val walletAddress: String? = null
)

/**
 * Data class for sending registration details to the backend.
 * Usually matches the FastAPI UserCreate schema.
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val pseudo: String, // 👈 New field for the username

    // KYC fields (optional or empty by default if the user chooses the Web3 Wallet path)
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("phone_number") val phoneNumber: String = "",
    val country: String = "",

    // Future integration for Web3
    @SerialName("wallet_address") val walletAddress: String? = null,
    val is_active: Boolean = true,
    val is_superuser: Boolean = false,
    val is_verified: Boolean = false
)