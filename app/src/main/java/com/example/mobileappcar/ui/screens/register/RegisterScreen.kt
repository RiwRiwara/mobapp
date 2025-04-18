package com.example.mobileappcar.ui.screens.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: RegisterViewModel = viewModel()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    val registerState by viewModel.registerState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register for Car Spa",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterViewModel.RegisterState.Error && username.isEmpty(),
            supportingText = {
                if (registerState is RegisterViewModel.RegisterState.Error) {
                    val errors = (registerState as RegisterViewModel.RegisterState.Error).messages
                    errors.firstOrNull { it.contains("Username") }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterViewModel.RegisterState.Error && email.isEmpty(),
            supportingText = {
                if (registerState is RegisterViewModel.RegisterState.Error) {
                    val errors = (registerState as RegisterViewModel.RegisterState.Error).messages
                    errors.firstOrNull { it.contains("Email") }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterViewModel.RegisterState.Error && password.isEmpty(),
            supportingText = {
                if (registerState is RegisterViewModel.RegisterState.Error) {
                    val errors = (registerState as RegisterViewModel.RegisterState.Error).messages
                    errors.firstOrNull { it.contains("Password") }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterViewModel.RegisterState.Error && firstName.isEmpty(),
            supportingText = {
                if (registerState is RegisterViewModel.RegisterState.Error) {
                    val errors = (registerState as RegisterViewModel.RegisterState.Error).messages
                    errors.firstOrNull { it.contains("First name") }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterViewModel.RegisterState.Error && lastName.isEmpty(),
            supportingText = {
                if (registerState is RegisterViewModel.RegisterState.Error) {
                    val errors = (registerState as RegisterViewModel.RegisterState.Error).messages
                    errors.firstOrNull { it.contains("Last name") }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Phone
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            isError = registerState is RegisterViewModel.RegisterState.Error && phone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches(),
            supportingText = {
                if (registerState is RegisterViewModel.RegisterState.Error) {
                    val errors = (registerState as RegisterViewModel.RegisterState.Error).messages
                    errors.firstOrNull { it.contains("phone") }?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.register(username, password, email, firstName, lastName, phone) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        ) {
            if (registerState is RegisterViewModel.RegisterState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Register", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.align(Alignment.End),
            enabled = registerState !is RegisterViewModel.RegisterState.Loading
        ) {
            Text("Already have an account? Login")
        }

        when (val state = registerState) {
            is RegisterViewModel.RegisterState.Error -> {
                Column {
                    state.messages.forEach { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            is RegisterViewModel.RegisterState.Success -> {
                LaunchedEffect(state) {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }
}