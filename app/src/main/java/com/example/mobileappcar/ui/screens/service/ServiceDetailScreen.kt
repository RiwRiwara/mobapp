package com.example.mobileappcar.ui.screens.service

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileappcar.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    navController: NavHostController,
    serviceId: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: ServiceViewModel = viewModel()
    val servicesState = viewModel.servicesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = servicesState.value) {
                        is ServiceViewModel.ServicesState.Success -> {
                            val service = state.services.find { it.id == serviceId }
                            Text(service?.name ?: "Service Detail")
                        }
                        else -> Text("Service Detail")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val state = servicesState.value) {
                is ServiceViewModel.ServicesState.Loading -> {
                    CircularProgressIndicator()
                }
                is ServiceViewModel.ServicesState.Success -> {
                    val service = state.services.find { it.id == serviceId }
                    if (service != null) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = service.description ?: "No description available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Price: ${service.price} THB",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Duration: ${service.duration} mins",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                navController.navigate(NavRoutes.BookingConfirm.replace("{serviceId}", serviceId.toString()))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirm")
                        }
                    } else {
                        Text("Service not found", color = MaterialTheme.colorScheme.error)
                    }
                }
                is ServiceViewModel.ServicesState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}