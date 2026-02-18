package com.trada.app.auth

import platform.Foundation.*
import platform.Security.*
import platform.CoreFoundation.*
import kotlinx.cinterop.*

actual class TokenStorage actual constructor(context: Any?) {

    actual fun saveAccessToken(token: String) {
        saveToKeychain("access_token", token)
    }

    actual fun getAccessToken(): String? {
        return getFromKeychain("access_token")
    }

    actual fun saveRefreshToken(token: String) {
        saveToKeychain("refresh_token", token)
    }

    actual fun getRefreshToken(): String? {
        return getFromKeychain("refresh_token")
    }

    actual fun clear() {
        deleteFromKeychain("access_token")
        deleteFromKeychain("refresh_token")
    }

    private fun saveToKeychain(key: String, value: String) {
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        deleteFromKeychain(key)

        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = key
        query[kSecValueData] = data

        SecItemAdd(query as CFDictionaryRef, null)
    }

    private fun getFromKeychain(key: String): String? {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = key
        query[kSecReturnData] = true
        query[kSecMatchLimit] = kSecMatchLimitOne

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)

            if (status == errSecSuccess) {
                val data = result.value as? NSData
                return data?.let { NSString.create(it, NSUTF8StringEncoding) as String? }
            }
        }
        return null
    }

    private fun deleteFromKeychain(key: String) {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrAccount] = key
        SecItemDelete(query as CFDictionaryRef)
    }
}