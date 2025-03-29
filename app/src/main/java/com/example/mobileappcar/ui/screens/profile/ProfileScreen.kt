//com/example/mobileappcar/ui/screens/ProfileScreen.kt
package com.example.mobileappcar.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = viewModel()
    val userState by viewModel.userState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "User Profile",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = userState) {
            is ProfileViewModel.UserState.Loading -> {
                CircularProgressIndicator()
            }
            is ProfileViewModel.UserState.Success -> {
                val user = state.user
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Username: ${user.username}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Role: ${user.role}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Phone: ${user.phone ?: "Not provided"}", // Handle null phone
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Joined: ${user.createdAt ?: "Unknown"}", // Handle null createdAt
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        ApiRepository.clearAuthToken() // Clear token on logout
                        navController.navigate(NavRoutes.Login) { // Use route name consistently
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Logout", fontSize = 16.sp, color = MaterialTheme.colorScheme.onError)
                }
            }
            is ProfileViewModel.UserState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchUserProfile() }, // Retry button
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            ApiRepository.clearAuthToken()
                            navController.navigate(NavRoutes.Login) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Login Again")
                    }
                }
            }
        }
    }
}