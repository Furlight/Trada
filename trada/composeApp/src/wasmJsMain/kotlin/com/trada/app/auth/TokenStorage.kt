package com.trada.app.auth

import kotlinx.browser.localStorage

actual class TokenStorage actual constructor(context: Any?) {
    // Pas de context sur le Web non plus

    actual fun saveAccessToken(token: String) {
        localStorage.setItem("access_token", token)
    }

    actual fun getAccessToken(): String? {
        return localStorage.getItem("access_token")
    }

    actual fun saveRefreshToken(token: String) {
        localStorage.setItem("refresh_token", token)
    }

    actual fun getRefreshToken(): String? {
        return localStorage.getItem("refresh_token")
    }

    actual fun clear() {
        localStorage.removeItem("access_token")
        localStorage.removeItem("refresh_token")
    }
}