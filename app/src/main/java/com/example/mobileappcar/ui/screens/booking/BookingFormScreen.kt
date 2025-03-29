package com.example.mobileappcar.ui.screens.booking

import androidx.compose.foundation.layout.*
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    navController: NavHostController,
    serviceId: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: ServiceViewModel = viewModel()
    val bookingState = viewModel.bookingState.collectAsState()

    // State for DatePicker and TimePicker
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var note by remember { mutableStateOf("") }

    // State for showing dialogs
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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

        // Date Picker Button
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "Select Date",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) { Text("Cancel") }
                }
            ) {
                val datePickerState = rememberDatePickerState()
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(16.dp)
                )
                LaunchedEffect(datePickerState.selectedDateMillis) {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / (1000 * 60 * 60 * 24))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time Picker Button
        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "Select Time",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = { showTimePicker = false }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showTimePicker = false }
                    ) { Text("Cancel") }
                }
            ) {
                val timePickerState = rememberTimePickerState()
                TimePicker(
                    state = timePickerState,
                    modifier = Modifier.padding(16.dp)
                )
                LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Note input (optional)
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit button
        Button(
            onClick = {
                val dateStr = selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val timeStr = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                if (dateStr != null && timeStr != null) {
                    viewModel.createBooking(serviceId, dateStr, timeStr, if (note.isBlank()) null else note)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedDate != null && selectedTime != null
        ) {
            Text("Confirm Booking")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Booking state feedback
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
            is ServiceViewModel.BookingState.Idle -> {
                // Do nothing
            }
        }
    }
}

// Helper composable for TimePickerDialog (since Material3 doesn’t provide it directly)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() }
    )
}