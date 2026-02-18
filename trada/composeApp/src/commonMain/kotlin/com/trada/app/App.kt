package com.trada.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trada.app.ui.LoginScreen
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.trada.app.ui.RegisterScreen
import com.trada.app.user.UserService
import trada.composeapp.generated.resources.Res
import trada.composeapp.generated.resources.compose_multiplatform
import com.trada.app.auth.TokenStorage
import com.trada.app.auth.UserResponse

// Create an Enum for simple navigation
enum class Screen { Login, Register, Main }

@Composable
fun App(tokenStorage: TokenStorage) {
    MaterialTheme {
        // Auto-login check: If we have a token, skip login and go straight to Main
        val startScreen = if (tokenStorage.getAccessToken() != null) Screen.Main else Screen.Login
        var currentScreen by remember { mutableStateOf(startScreen) }

        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Login -> LoginScreen(
                    tokenStorage = tokenStorage,
                    onLoginSuccess = { token ->
                        currentScreen = Screen.Main
                    },
                    onGoToRegister = { currentScreen = Screen.Register }
                )

                Screen.Register -> RegisterScreen(
                    onRegisterSuccess = { currentScreen = Screen.Login },
                    onBackToLogin = { currentScreen = Screen.Login }
                )

                Screen.Main -> MainAppContent(
                    tokenStorage = tokenStorage,
                    onLogout = {
                        tokenStorage.clear()
                        currentScreen = Screen.Login
                    }
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    tokenStorage: TokenStorage,
    onLogout: () -> Unit
) {
    // State to hold user data and loading status
    var user by remember { mutableStateOf<UserResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Instantiate UserService
    val userService = remember { UserService(tokenStorage) }
    val scope = rememberCoroutineScope()

    // ⚡️ Load user profile when this screen appears
    LaunchedEffect(Unit) {
        try {
            user = userService.getCurrentUser()
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to load profile: ${e.message}"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Trada Home",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            // Show loading spinner while fetching the profile
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Loading profile...")
        }
        else if (errorMessage != null) {
            // Error Case: Token expired or network issue
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout) {
                Text("Logout & Retry")
            }
        }
        else {
            // ✅ Success Case: Display User Info & KYC Data
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.Start // Left-aligned looks cleaner for lists
                ) {
                    // --- Header ---
                    Text(
                        text = "Welcome, ${user?.pseudo ?: "Trader"}!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Basic Account Info ---
                    Text("📧 Email: ${user?.email}", style = MaterialTheme.typography.bodyLarge)
                    Text("🆔 Account ID: ${user?.id}", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider() // Visual separator line
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- KYC Information ---
                    Text("📋 KYC Profile", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Safely format the full name, or show "Not provided" if both are null/empty
                    val fullName = listOfNotNull(user?.firstName, user?.lastName)
                        .joinToString(" ")
                        .takeIf { it.isNotBlank() } ?: "Not provided"

                    Text("👤 Name: $fullName", style = MaterialTheme.typography.bodyMedium)
                    Text("🌍 Country: ${user?.country ?: "Not provided"}", style = MaterialTheme.typography.bodyMedium)
                    Text("📱 Phone: ${user?.phoneNumber ?: "Not provided"}", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- Status Badges ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (user?.is_active == true) {
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text("Active", modifier = Modifier.padding(6.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (user?.is_verified == true) {
                            Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                                Text("Verified KYC", modifier = Modifier.padding(6.dp), color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        } else {
                            Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                Text("Unverified", modifier = Modifier.padding(6.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}