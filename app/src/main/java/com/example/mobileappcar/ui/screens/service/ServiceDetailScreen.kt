package com.example.mobileappcar.ui.screens.service

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
    val availableTimesState = viewModel.availableTimesState.collectAsState()
    val bookingState = viewModel.bookingState.collectAsState()
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }

    LaunchedEffect(serviceId) {
        viewModel.fetchAvailableTimes(serviceId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = servicesState.value) {
                        is ServiceViewModel.ServicesState.Success -> {
                            val service = state.services.find { it.id == serviceId }
                            Text(
                                text = service?.name ?: "Loading...",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        else -> Text("Loading...")
                    }
                },
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
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Select a Time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        when (val timesState = availableTimesState.value) {
                            is ServiceViewModel.AvailableTimesState.Loading -> {
                                CircularProgressIndicator()
                            }
                            is ServiceViewModel.AvailableTimesState.Success -> {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 200.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(timesState.availableTimes) { time ->
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
                                    text = "Error loading times: ${timesState.message}",
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

                        // Confirm and Save Button
                        Button(
                            onClick = {
                                selectedTime?.let { time ->
                                    viewModel.createBooking(serviceId, time, if (note.isBlank()) null else note)
                                } ?: run {
                                    Log.w("ServiceDetailScreen", "No time selected")
                                }
                            },
                            enabled = selectedTime != null && bookingState.value !is ServiceViewModel.BookingState.Loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (bookingState.value is ServiceViewModel.BookingState.Loading) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Confirm Booking")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Booking State Feedback
                        when (val booking = bookingState.value) {
                            is ServiceViewModel.BookingState.Success -> {
                                Text(
                                    text = "Booking Confirmed! ID: ${booking.booking.id}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                LaunchedEffect(booking) {
                                    navController.navigate(NavRoutes.Bookings) {
                                        popUpTo(NavRoutes.Services) { inclusive = false }
                                    }
                                }
                            }
                            is ServiceViewModel.BookingState.Error -> {
                                Text(
                                    text = "Error: ${booking.message}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            is ServiceViewModel.BookingState.Loading -> {}
                            is ServiceViewModel.BookingState.Idle -> {}
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