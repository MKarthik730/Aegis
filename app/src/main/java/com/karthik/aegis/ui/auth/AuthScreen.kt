package com.karthik.aegis.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karthik.aegis.viewmodel.AuthUiState
import com.karthik.aegis.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onAuthSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Logo / Title
        Text(
            "🛡️",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            "Aegis",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            "Family Safety & Emergency Response",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, "Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            enabled = !uiState.isLoading,
            singleLine = true
        )

        // Display Name (Sign Up only)
        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                enabled = !uiState.isLoading,
                singleLine = true
            )
        }

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, "Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            enabled = !uiState.isLoading,
            singleLine = true
        )

        // Error Message
        uiState.error?.let {
            Surface(
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp
                )
            }
        }

        // Sign In / Sign Up Button
        Button(
            onClick = {
                if (isSignUp) {
                    viewModel.createAccountWithEmail(email, password, displayName)
                } else {
                    viewModel.signInWithEmail(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = email.isNotEmpty() && password.isNotEmpty() && (!isSignUp || displayName.isNotEmpty()) && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text(
                    if (isSignUp) "Create Account" else "Sign In",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Between Sign In / Sign Up
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isSignUp) "Already have an account?" else "Don't have an account?",
                fontSize = 12.sp
            )

            TextButton(
                onClick = { isSignUp = !isSignUp },
                modifier = Modifier.padding(start = 4.dp),
                enabled = !uiState.isLoading
            ) {
                Text(
                    if (isSignUp) "Sign In" else "Sign Up",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Demo Credentials
        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    "Demo Credentials",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Email: demo@aegis.app",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Text(
                    "Pass: demo123456",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        email = "demo@aegis.app"
                        password = "demo123456"
                        displayName = "Demo User"
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Use Demo", fontSize = 10.sp)
                }
            }
        }
    }
}
