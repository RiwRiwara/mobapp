package com.example.mobileappcar.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLoginSuccess: (Boolean) -> Unit = {} // Callback to update login state
) {
    val viewModel: LoginViewModel = viewModel()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login to Car Spa",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            enabled = loginState !is LoginViewModel.LoginState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            enabled = loginState !is LoginViewModel.LoginState.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp),
            enabled = loginState !is LoginViewModel.LoginState.Loading
        ) {
            if (loginState is LoginViewModel.LoginState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.align(Alignment.End),
            enabled = loginState !is LoginViewModel.LoginState.Loading
        ) {
            Text("Don’t have an account? Register")
        }

        when (val state = loginState) {
            is LoginViewModel.LoginState.Success -> {
                LaunchedEffect(state) {
                    onLoginSuccess(true) // Notify login success
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            is LoginViewModel.LoginState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
            else -> {}
        }
    }
}