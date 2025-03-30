package com.example.mobileappcar.ui.screens.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun BookingFormScreen(
    navController: NavHostController,
    serviceId: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: ServiceViewModel = viewModel()
    val bookingState = viewModel.bookingState.collectAsState()
    val availableTimesState = viewModel.availableTimesState.collectAsState()
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(serviceId) {
        viewModel.fetchAvailableTimes(serviceId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Book Service",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Available Times Selection
        Text(
            text = "Select a Time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        when (val state = availableTimesState.value) {
            is ServiceViewModel.AvailableTimesState.Loading -> {
                CircularProgressIndicator()
            }
            is ServiceViewModel.AvailableTimesState.Success -> {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.availableTimes) { time ->
                        TimeItem(
                            time = time,
                            isSelected = selectedTime == time,
                            onSelect = { selectedTime = time }
                        )
                    }
                }
            }
            is ServiceViewModel.AvailableTimesState.Error -> {
                Text(
                    text = "Error loading times: ${state.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Input
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                selectedTime?.let { time ->
                    viewModel.createBooking(serviceId, time, if (note.isBlank()) null else note)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedTime != null
        ) {
            Text("Confirm Booking")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Booking State Feedback
        when (val state = bookingState.value) {
            is ServiceViewModel.BookingState.Loading -> {
                CircularProgressIndicator()
            }
            is ServiceViewModel.BookingState.Success -> {
                Text(
                    text = "Booking created successfully! ID: ${state.booking.id}",
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

@Composable
fun TimeItem(
    time: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = { onSelect() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}