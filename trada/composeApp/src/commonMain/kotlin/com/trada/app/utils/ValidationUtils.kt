package com.trada.app.utils

object ValidationUtils {
    // --- REGEX RULES ---
    fun isValidEmail(email: String): Boolean =
        email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex())

    fun isValidPassword(password: String): Boolean =
        password.matches("^(?=.*[0-9])(?=.*[A-Z]).{8,}\$".toRegex())

    fun isValidPhone(phone: String): Boolean =
        phone.isEmpty() || phone.matches("^\\+?[0-9]{8,15}\$".toRegex())

    // --- BACKEND ERROR PARSER ---
    /**
     * Extracts a human-readable message from Pydantic/FastAPI JSON errors.
     */
    fun parseBackendError(jsonString: String): String {
        return try {
            when {
                // Case 1: Pydantic Validation Error (detailed list)
                jsonString.contains("\"msg\":") -> {
                    val regex = "\"msg\":\"([^\"]+)\"".toRegex()
                    regex.find(jsonString)?.groupValues?.get(1)?.replace("Value error, ", "")
                        ?: "Validation failed"
                }
                // Case 2: Simple HTTPException (detail: "message")
                jsonString.contains("\"detail\":\"") -> {
                    val regex = "\"detail\":\"([^\"]+)\"".toRegex()
                    regex.find(jsonString)?.groupValues?.get(1) ?: "An error occurred"
                }
                else -> jsonString.removePrefix("Error: ")
            }
        } catch (e: Exception) {
            "Connection error"
        }
    }
}