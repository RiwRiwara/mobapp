package com.example.mobileappcar.ui.screens.booking

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.mobileappcar.ui.components.BookingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingListScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: BookingListViewModel = viewModel()
    val bookingsState = viewModel.bookingsState.collectAsState()

// Get the refresh signal from savedStateHandle
    val refreshSignal = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<Boolean>("refresh", false)
        ?.collectAsState(initial = false)

    // Refresh bookings when the signal changes to true
    LaunchedEffect(refreshSignal) {
        if (refreshSignal?.value == true) {
            viewModel.fetchBookings()
            // Reset the signal after refreshing
            navController.currentBackStackEntry?.savedStateHandle?.set("refresh", false)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Bookings",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = { viewModel.fetchBookings() },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Refresh")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (val state = bookingsState.value) {
            is BookingListViewModel.BookingsState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is BookingListViewModel.BookingsState.Success -> {
                if (state.bookings.isEmpty()) {
                    Text(
                        text = "No bookings found",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn {
                        items(state.bookings) { booking ->
                            BookingItem(
                                booking = booking,
                                onClick = {
                                    if (booking.id > 0) {
                                        navController.navigate("bookingDetail/${booking.id}")
                                    } else {
                                        Log.e("BookingListScreen", "Invalid booking ID: ${booking.id}")
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            is BookingListViewModel.BookingsState.Error -> {
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
                    Button(onClick = { viewModel.fetchBookings() }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}