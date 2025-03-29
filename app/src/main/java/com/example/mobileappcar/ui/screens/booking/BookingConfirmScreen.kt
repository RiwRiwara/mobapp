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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingConfirmScreen(
    navController: NavHostController,
    serviceId: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: ServiceViewModel = viewModel()
    val bookingState = viewModel.bookingState.collectAsState()

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
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

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            isError = date.isNotEmpty() && !date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (HH:MM)") },
            modifier = Modifier.fillMaxWidth(),
            isError = time.isNotEmpty() && !time.matches(Regex("^([01]\\d|2[0-3]):([0-5]\\d)$"))
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$")) &&
                    time.matches(Regex("^([01]\\d|2[0-3]):([0-5]\\d)$"))
                ) {
                    viewModel.createBooking(serviceId, date, time, if (note.isBlank()) null else note)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = date.isNotEmpty() && time.isNotEmpty()
        ) {
            Text("Confirm Booking")
        }

        Spacer(modifier = Modifier.height(16.dp))

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