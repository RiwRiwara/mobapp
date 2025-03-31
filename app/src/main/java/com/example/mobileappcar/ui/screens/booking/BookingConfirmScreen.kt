package com.example.mobileappcar.ui.screens.booking

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileappcar.ui.screens.service.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmScreen(
    navController: NavHostController,
    serviceId: Int,
    time: String?,
    modifier: Modifier = Modifier
) {
    val viewModel: ServiceViewModel = viewModel()
    val bookingState = viewModel.bookingState.collectAsState()
    var note by remember { mutableStateOf("") }

    // Pre-select the time passed from ServiceDetailScreen, default to null if not provided
    var selectedTime by remember { mutableStateOf(time) }

    if (selectedTime == null) {
        Log.w("BookingConfirmScreen", "No time provided in navigation")
        // Optionally navigate back or show an error
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirm Your Booking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
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
            Text(
                text = "Confirm Your Booking",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display selected service and time
            Text(
                text = "Service ID: $serviceId",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Selected Time: $selectedTime",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm and Save Button
            Button(
                onClick = {
                    selectedTime?.let { time ->
                        viewModel.createBooking(serviceId, time, if (note.isBlank()) null else note)
                    } ?: run {
                        Log.e("BookingConfirmScreen", "Selected time is null when confirming")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedTime != null
            ) {
                Text("Save Booking")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Booking State Feedback
            when (val state = bookingState.value) {
                is ServiceViewModel.BookingState.Loading -> {
                    CircularProgressIndicator()
                }
                is ServiceViewModel.BookingState.Success -> {
                    Text(
                        text = "Booking Confirmed! ID: ${state.booking.id}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LaunchedEffect(state) {
                        navController.navigate("bookings") {
                            popUpTo("services") { inclusive = false }
                        }
                    }
                }
                is ServiceViewModel.BookingState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is ServiceViewModel.BookingState.Idle -> {}
            }
        }
    }
}