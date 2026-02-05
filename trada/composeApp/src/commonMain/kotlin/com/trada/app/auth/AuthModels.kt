package com.trada.app.auth

import kotlinx.serialization.Serializable

/**
 * Data class representing the successful login response from FastAPI.
 */
@Serializable
data class TokenResponse(
    val access_token: String,
    val token_type: String
)

/**
 * Data class representing basic user information.
 */
@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val is_active: Boolean
)