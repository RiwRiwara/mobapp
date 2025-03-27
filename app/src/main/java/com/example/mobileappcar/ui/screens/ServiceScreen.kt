package com.example.mobileappcar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.mobileappcar.model.Service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: ServiceViewModel = viewModel()
    val servicesState = viewModel.servicesState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select a Service",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (val state = servicesState.value) {
            is ServiceViewModel.ServicesState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ServiceViewModel.ServicesState.Success -> {
                LazyColumn {
                    items(state.services) { service ->
                        ServiceCard(service, navController)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            is ServiceViewModel.ServicesState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchServices() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: Service, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Price: ${service.price} THB", // Updated to Thai Baht
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Duration: ${service.duration} mins",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = {
                    navController.navigate("bookingConfirm/${service.id}")
                },
                modifier = Modifier.height(36.dp)
            ) {
                Text("Book Now", fontSize = 14.sp)
            }
        }
    }
}