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
import trada.composeapp.generated.resources.Res
import trada.composeapp.generated.resources.compose_multiplatform

// Create an Enum for simple navigation
enum class Screen { Login, Register, Main }

@Composable
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(Screen.Login) }
        var authToken by remember { mutableStateOf<String?>(null) }

        Surface(modifier = Modifier.fillMaxSize()) {
            when (currentScreen) {
                Screen.Login -> LoginScreen(
                    onLoginSuccess = { token ->
                        authToken = token
                        currentScreen = Screen.Main
                    },
                    onGoToRegister = { currentScreen = Screen.Register }
                )
                Screen.Register -> RegisterScreen(
                    onRegisterSuccess = { currentScreen = Screen.Login },
                    onBackToLogin = { currentScreen = Screen.Login }
                )
                Screen.Main -> MainAppContent(onLogout = {
                    authToken = null
                    currentScreen = Screen.Login
                })
            }
        }
    }
}

@Composable
fun MainAppContent(onLogout: () -> Unit) {
    var showContent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Welcome to Trada",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(16.dp)
        )

        Button(onClick = { showContent = !showContent }) {
            Text(if (showContent) "Hide Secret" else "Click me!")
        }

        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Simple Logout button for testing
        TextButton(onClick = onLogout) {
            Text("Logout", color = MaterialTheme.colorScheme.error)
        }
    }
}