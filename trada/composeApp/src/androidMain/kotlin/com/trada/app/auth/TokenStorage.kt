package com.trada.app.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual class TokenStorage actual constructor(context: Any?) {

    // We cast the generic 'Any?' to a real Android 'Context'.
    // This is safe because we will pass the Application Context from the Android side.
    private val androidContext = context as? Context
        ?: throw IllegalArgumentException("Android TokenStorage requires a Context")

    private val sharedPreferences: SharedPreferences by lazy {
        // 1. Generate or retrieve the Master Key (hardware-backed if available)
        val masterKey = MasterKey.Builder(androidContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // 2. Open (or create) the encrypted file
        EncryptedSharedPreferences.create(
            androidContext,
            "secure_tokens_prefs", // Filename
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    actual fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    actual fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    actual fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    actual fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}