package com.trada.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.unit.dp
import com.trada.app.auth.AuthService
import com.trada.app.utils.ValidationUtils
import kotlinx.coroutines.launch
import com.trada.app.utils.PhoneSpacingTransformation

private enum class RegisterStep {
    Credentials,  // Email & Password
    Pseudo,       // Username selection
    MethodChoice, // Choice between KYC or Web3
    PersonalInfo, // Identity (First/Last name)
    Address       // Phone & Country
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    // --- GLOBAL FORM STATES ---
    var step by remember { mutableStateOf(RegisterStep.Credentials) }

    // Form Data
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var pseudo by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var phonePrefix by remember { mutableStateOf("+33") }
    var prefixExpanded by remember { mutableStateOf(false) }
    var country by remember { mutableStateOf("") }
    var connectedWallet by remember { mutableStateOf<String?>(null) }

    // UI Logic
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val authService = remember { AuthService() }

    // --- DYNAMIC VALIDATION LOGIC ---
    val isEmailValid = ValidationUtils.isValidEmail(email)
    val isPasswordValid = ValidationUtils.isValidPassword(password)
    val passwordsMatch = password == confirmPassword && password.isNotEmpty()
    val isPseudoValid = pseudo.length >= 3

    // 🛡️ Require EXACTLY 9 digits for the phone number
    val isPhoneValid = phoneNumber.length == 9

    // --- FINAL SUBMISSION FUNCTION ---
    fun submitRegistration() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val fullPhoneNumber = "$phonePrefix$phoneNumber".replace(" ", "")

                authService.register(
                    email = email,
                    pass = password,
                    pseudo = pseudo,
                    firstName = firstName,
                    lastName = lastName,
                    phone = fullPhoneNumber,
                    country = country,
                    walletAddress = connectedWallet
                )
                isLoading = false
                onRegisterSuccess()
            } catch (e: Exception) {
                isLoading = false
                errorMessage = ValidationUtils.parseBackendError(e.message ?: "Unknown error")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding() // 🚀 FIX: Prevents content from being hidden under the camera
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 🎨 SEGMENTED PROGRESS BAR (Refined Design) ---
        val allSteps = RegisterStep.values()
        val currentStepIndex = allSteps.indexOf(step)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // 1. Centered Step Indicator
            Text(
                text = "STEP ${currentStepIndex + 1} / ${allSteps.size}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
            )

            // 2. Elegant Thin Segments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allSteps.forEachIndexed { index, _ ->
                    val isActive = index <= currentStepIndex
                    val color by animateColorAsState(
                        targetValue = if (isActive) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.5f),
                        label = "segmentColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp) // Thinner for a premium look
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
            }
        }

        // --- TITLE ---
        Text(
            text = when(step) {
                RegisterStep.Credentials -> "Create an Account"
                RegisterStep.Pseudo -> "Choose a Username"
                RegisterStep.MethodChoice -> "Identity Verification"
                RegisterStep.PersonalInfo -> "Personal Information"
                RegisterStep.Address -> "Location Details"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- DYNAMIC STEP CONTENT (The Form) ---
        Box(modifier = Modifier.weight(1f)) {
            when (step) {
                RegisterStep.Credentials -> {
                    Column {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = email.isNotEmpty() && !isEmailValid,
                            supportingText = { if (email.isNotEmpty() && !isEmailValid) Text("Invalid email format") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            isError = password.isNotEmpty() && !isPasswordValid,
                            supportingText = { if (password.isNotEmpty() && !isPasswordValid) Text("8+ chars, 1 Uppercase, 1 Number") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                            supportingText = { if (confirmPassword.isNotEmpty() && !passwordsMatch) Text("Passwords do not match") }
                        )
                    }
                }
                RegisterStep.Pseudo -> {
                    Column {
                        Text("How should we call you on the platform?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = pseudo,
                            onValueChange = { pseudo = it; errorMessage = null },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = pseudo.isNotEmpty() && !isPseudoValid,
                            supportingText = { if (pseudo.isNotEmpty() && !isPseudoValid) Text("Minimum 3 characters required") }
                        )
                    }
                }
                RegisterStep.MethodChoice -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Choose how you want to verify your identity:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { step = RegisterStep.PersonalInfo },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📄 Standard KYC Form", fontWeight = FontWeight.Bold)
                                Text("Manually enter your personal details.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                connectedWallet = "HN7cABqLq46Es1jh92dQQisAq662SmxELLLsHHe4YWrH"
                                submitRegistration()
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("👻 Connect Phantom Wallet", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("Fast-track registration using Web3.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
                RegisterStep.PersonalInfo -> {
                    Column {
                        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
                    }
                }
                RegisterStep.Address -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Country Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        val countries = listOf("France", "United States", "United Kingdom", "Canada", "Germany", "Spain", "Italy", "Belgium", "Switzerland")
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = country,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Country") },
                                leadingIcon = { Icon(Icons.Default.Public, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                countries.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(text = selection) },
                                        onClick = { country = selection; expanded = false; errorMessage = null },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                        // Phone Number Field
                        val prefixes = listOf("France" to "+33", "United States" to "+1", "United Kingdom" to "+44", "Germany" to "+49", "Spain" to "+34", "Italy" to "+39", "Belgium" to "+32", "Switzerland" to "+41")
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }.take(9)
                                phoneNumber = digitsOnly
                                errorMessage = null
                            },
                            label = { Text("Phone Number") },
                            placeholder = { Text("6 12 34 56 78") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            isError = phoneNumber.isNotEmpty() && !isPhoneValid,
                            visualTransformation = PhoneSpacingTransformation(),
                            leadingIcon = {
                                Box {
                                    Row(
                                        modifier = Modifier.clickable { prefixExpanded = true }.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = phonePrefix, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Prefix")
                                    }
                                    DropdownMenu(expanded = prefixExpanded, onDismissRequest = { prefixExpanded = false }) {
                                        prefixes.forEach { (countryName, code) ->
                                            DropdownMenuItem(
                                                text = { Text("$code ($countryName)") },
                                                onClick = { phonePrefix = code; prefixExpanded = false }
                                            )
                                        }
                                    }
                                }
                            },
                            supportingText = {
                                if (phoneNumber.isNotEmpty() && !isPhoneValid) {
                                    Text("9 digits required (missing ${9 - phoneNumber.length})", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- ERROR DISPLAY ---
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // --- NAVIGATION BUTTONS ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step != RegisterStep.Credentials) {
                OutlinedButton(onClick = {
                    step = when (step) {
                        RegisterStep.Address -> RegisterStep.PersonalInfo
                        RegisterStep.PersonalInfo -> RegisterStep.MethodChoice
                        RegisterStep.MethodChoice -> RegisterStep.Pseudo
                        RegisterStep.Pseudo -> RegisterStep.Credentials
                        else -> RegisterStep.Credentials
                    }
                }) { Text("Back") }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (step != RegisterStep.MethodChoice) {
                Button(
                    onClick = {
                        when (step) {
                            RegisterStep.Credentials -> step = RegisterStep.Pseudo
                            RegisterStep.Pseudo -> step = RegisterStep.MethodChoice
                            RegisterStep.PersonalInfo -> step = RegisterStep.Address
                            RegisterStep.Address -> submitRegistration()
                            else -> {}
                        }
                    },
                    enabled = !isLoading && when(step) {
                        RegisterStep.Credentials -> isEmailValid && isPasswordValid && passwordsMatch
                        RegisterStep.Pseudo -> isPseudoValid
                        RegisterStep.PersonalInfo -> firstName.isNotBlank() && lastName.isNotBlank()
                        RegisterStep.Address -> country.isNotBlank() && isPhoneValid
                        else -> true
                    }
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text(if (step == RegisterStep.Address) "Finish" else "Next")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (step == RegisterStep.Credentials) {
            TextButton(onClick = onBackToLogin) { Text("Already have an account? Login") }
        }
    }
}