package com.trada.app.auth

/**
 * Shared interface for secure token storage.
 * IMPORTANT: The constructor accepts a 'context' (optional, default is null).
 * This allows Android to pass its Context/Activity, while keeping other platforms (iOS, Web) unaffected.
 */
expect class TokenStorage(context: Any? = null) {
    fun saveAccessToken(token: String)
    fun getAccessToken(): String?
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clear()
}