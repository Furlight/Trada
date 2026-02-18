package com.trada.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.trada.app.auth.AuthService
import com.trada.app.auth.TokenStorage
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    tokenStorage: TokenStorage,
    onLoginSuccess: (String) -> Unit,
    onGoToRegister: () -> Unit
) {
    // --- State Management ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Coroutine scope for handling suspend functions (network calls)
    val scope = rememberCoroutineScope()

    // Instantiate the AuthService once
    val authService = remember { AuthService() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text("Trada Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Disable input while loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Disable input while loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error Message Display
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Login Button
        Button(
            onClick = {
                // 1. Reset state and show loading indicator
                isLoading = true
                errorMessage = null

                // 2. Launch background coroutine for the network call
                scope.launch {
                    try {
                        // 3. Call FastAPI to get the tokens
                        val response = authService.login(email, password)

                        // 4. Securely store the tokens (Task 1.4 Hardening)
                        tokenStorage.saveAccessToken(response.access_token)
                        response.refresh_token?.let {
                            tokenStorage.saveRefreshToken(it)
                        }

                        // 5. Hide loading and trigger navigation
                        isLoading = false
                        onLoginSuccess(response.access_token)

                    } catch (e: Exception) {
                        // 6. Handle errors (e.g., incorrect password, network issue)
                        isLoading = false
                        errorMessage = "Login failed: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            // Prevent clicking if fields are empty or already loading
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
        ) {
            if (isLoading) {
                // Show spinner during the network request
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation to Register Screen
        TextButton(
            onClick = onGoToRegister,
            enabled = !isLoading
        ) {
            Text("Don't have an account? Create one")
        }
    }
}