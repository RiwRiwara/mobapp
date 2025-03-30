package com.example.mobileappcar.ui.screens.payment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.Booking
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavHostController,
    bookingId: Int,
    modifier: Modifier = Modifier
) {
    val viewModel: PaymentViewModel = viewModel()
    val paymentState = viewModel.paymentState.collectAsState()
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("QR Code") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var booking by remember { mutableStateOf<Booking?>(null) }
    val scope = rememberCoroutineScope()
    val apiRepository = ApiRepository()

    // Fetch booking details to check payment status
    LaunchedEffect(bookingId) {
        val result = apiRepository.getBookingDetails(bookingId)
        result.onSuccess { fetchedBooking ->
            booking = fetchedBooking
            Log.d("PaymentScreen", "Fetched booking: $fetchedBooking")
        }.onFailure { exception ->
            errorMessage = "Failed to load booking: ${exception.message}"
            Log.e("PaymentScreen", "Booking fetch failed", exception)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (uri == null) errorMessage = "Failed to select an image."
        else Log.d("PaymentScreen", "Image URI selected: $uri")
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) galleryLauncher.launch("image/*")
        else errorMessage = "Permission denied. Please grant media access in Settings."
    }

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

            // Show existing payment slip if completed
            booking?.paymentImage?.let { imagePath ->
                if (booking?.paymentStatus == "completed") {
                    Text(
                        text = "Payment Completed",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter("http://10.0.2.2:3000$imagePath"), // Adjust URL as needed
                        contentDescription = "Payment Slip",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This booking has already been paid.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    return@Column // Exit early if payment is completed
                }
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (THB)") },
                modifier = Modifier.fillMaxWidth(),
                isError = amount.isNotEmpty() && amount.toFloatOrNull() == null,
                enabled = booking?.paymentId == null || booking?.paymentStatus != "completed"
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
                    readOnly = true,
                    enabled = booking?.paymentId == null || booking?.paymentStatus != "completed"
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

            // Image Upload Button
            Button(
                onClick = {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                        galleryLauncher.launch("image/*")
                    } else {
                        permissionLauncher.launch(permission)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = booking?.paymentId == null || booking?.paymentStatus != "completed"
            ) {
                Text("Upload Payment Slip")
            }

            // Display Selected Image
            imageUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Payment Slip",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    amount.toFloatOrNull()?.let { amt ->
                        scope.launch {
                            val imageBase64 = imageUri?.let { uriToBase64(context, it) }
                            viewModel.createPayment(bookingId, amt, paymentMethod, imageBase64)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotEmpty() &&
                        amount.toFloatOrNull() != null &&
                        (paymentMethod != "QR Code" || imageUri != null) &&
                        (booking?.paymentId == null || booking?.paymentStatus != "completed")
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

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun uriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: throw IllegalStateException("Empty image data")
        inputStream.close()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        Log.d("PaymentScreen", "Base64 generated: length=${base64.length}")
        base64
    } catch (e: Exception) {
        Log.e("PaymentScreen", "Failed to convert URI to Base64", e)
        throw e
    }
}