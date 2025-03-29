package com.example.mobileappcar.ui.screens.booking

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlinx.coroutines.launch

@Composable
fun BookingDetailScreen(bookingId: Int, navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: BookingDetailViewModel = viewModel(factory = BookingDetailViewModelFactory(bookingId))
    val bookingState = viewModel.bookingState.collectAsState()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageBase64 by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPayment by remember { mutableStateOf(false) }
    var submitPayment by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (uri == null) errorMessage = "Failed to select an image."
        else Log.d("BookingDetailScreen", "Image URI selected: $uri")
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) galleryLauncher.launch("image/*")
        else errorMessage = "Permission denied. Please grant media access in Settings."
    }

    LaunchedEffect(submitPayment) {
        if (submitPayment && imageUri != null) {
            try {
                val booking = (bookingState.value as? BookingDetailViewModel.BookingState.Success)?.booking
                if (booking != null) {
                    val base64Image = uriToBase64(context, imageUri!!)
                    Log.d("BookingDetailScreen", "Submitting payment with Base64 length: ${base64Image.length}")
                    val result = viewModel.createPayment(
                        bookingId = booking.id,
                        amount = booking.price ?: 0f,
                        paymentMethod = "QR Code",
                        image = base64Image
                    )
                    result.onSuccess {
                        uploadedImageBase64 = base64Image
                        imageUri = null
                        errorMessage = "Payment submitted successfully!"
                        viewModel.fetchBookingDetails()
                        navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    }.onFailure { exception ->
                        errorMessage = "Payment failed: ${exception.message}"
                        Log.e("BookingDetailScreen", "Payment submission failed", exception)
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error processing image: ${e.message}"
                Log.e("BookingDetailScreen", "Image processing error", e)
            } finally {
                submitPayment = false
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            when (val state = bookingState.value) {
                is BookingDetailViewModel.BookingState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
                }
                is BookingDetailViewModel.BookingState.Success -> {
                    val booking = state.booking
                    hasPayment = booking.paymentId != null || uploadedImageBase64 != null

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Booking Details",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(label = "ID", value = "${booking.id}")
                            DetailRow(label = "Date", value = "${booking.date} ${booking.time}")
                            DetailRow(label = "Status", value = booking.status)
                            DetailRow(label = "Service", value = booking.serviceName ?: "Unknown")
                            DetailRow(label = "Price", value = "${booking.price ?: "N/A"}")
                            booking.paymentId?.let { id -> DetailRow(label = "Payment ID", value = "$id") }
                            booking.paymentStatus?.let { status -> DetailRow(label = "Payment Status", value = status) }
                        }
                    }

                    (booking.paymentImage ?: uploadedImageBase64)?.let { imageData ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Payment Slip",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val imageUrl = if (imageData.startsWith("/uploads")) {
                                    "http://10.0.2.2:3000$imageData"
                                } else {
                                    "data:image/jpeg;base64,$imageData"
                                }
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageUrl),
                                    contentDescription = "Payment Slip",
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }

                    if (!hasPayment && booking.status == "waiting") {
                        Spacer(modifier = Modifier.height(16.dp))
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
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Pay", fontSize = 16.sp)
                        }

                        imageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Selected Payment Slip",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Selected Payment Slip",
                                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { submitPayment = true },
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Submit Payment", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = msg,
                            color = if (msg.contains("successfully")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                    }
                }
                is BookingDetailViewModel.BookingState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun uriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes() ?: throw IllegalStateException("Empty image data")
        inputStream.close()
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP) // Use NO_WRAP to avoid newlines
        Log.d("BookingDetailScreen", "Base64 generated: length=${base64.length}, startsWith=${base64.take(20)}")
        base64
    } catch (e: Exception) {
        Log.e("BookingDetailScreen", "Failed to convert URI to Base64", e)
        throw e
    }
}

