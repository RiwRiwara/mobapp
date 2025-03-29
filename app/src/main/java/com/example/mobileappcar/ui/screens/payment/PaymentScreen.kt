package com.example.mobileappcar.ui.screens.payment

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavHostController,
    bookingId: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: PaymentViewModel = viewModel()
    val paymentState = viewModel.paymentState.collectAsState()

    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("QR Code") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") }
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
                text = "Payment for Booking ID: $bookingId",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (THB)") },
                modifier = Modifier.fillMaxWidth(),
                isError = amount.isNotEmpty() && amount.toFloatOrNull() == null
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { /* Expandable dropdown if needed */ }
            ) {
                TextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true
                )
                ExposedDropdownMenu(
                    expanded = false,
                    onDismissRequest = { }
                ) {
                    DropdownMenuItem(
                        text = { Text("QR Code") },
                        onClick = { paymentMethod = "QR Code" }
                    )
                    DropdownMenuItem(
                        text = { Text("Credit Card") },
                        onClick = { paymentMethod = "Credit Card" }
                    )
                    DropdownMenuItem(
                        text = { Text("Cash") },
                        onClick = { paymentMethod = "Cash" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    amount.toFloatOrNull()?.let { amt ->
                        viewModel.createPayment(bookingId, amt, paymentMethod)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotEmpty() && amount.toFloatOrNull() != null
            ) {
                Text("Submit Payment")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = paymentState.value) {
                is PaymentViewModel.PaymentState.Loading -> {
                    CircularProgressIndicator()
                }
                is PaymentViewModel.PaymentState.Success -> {
                    Text(
                        text = "Payment successful! ID: ${state.payment.id}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LaunchedEffect(state) {
                        navController.navigate("bookings") {
                            popUpTo("services") { inclusive = false }
                        }
                    }
                }
                is PaymentViewModel.PaymentState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is PaymentViewModel.PaymentState.Idle -> {}
            }
        }
    }
}