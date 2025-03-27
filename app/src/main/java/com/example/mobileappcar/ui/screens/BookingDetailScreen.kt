package com.example.mobileappcar.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Base64
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.mobileappcar.data.repository.ApiRepository
import com.example.mobileappcar.model.Booking
import com.example.mobileappcar.model.Payment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun BookingDetailScreen(bookingId: Int, navController: NavHostController, modifier: Modifier = Modifier) {
    val viewModel: BookingDetailViewModel = viewModel(factory = BookingDetailViewModelFactory(bookingId))
    val bookingState = viewModel.bookingState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Selected image before upload
    var uploadedImageBase64 by remember { mutableStateOf<String?>(null) } // Local copy of uploaded image
    var paymentStatus by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasPayment by remember { mutableStateOf(false) } // Track if payment exists

    // Launcher for picking an image from gallery
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (uri == null) {
            errorMessage = "Failed to select an image."
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            errorMessage = "Permission denied. Please grant media access in Settings to upload a payment slip."
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (val state = bookingState.value) {
                is BookingDetailViewModel.BookingState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(32.dp)
                    )
                }
                is BookingDetailViewModel.BookingState.Success -> {
                    val booking = state.booking
                    paymentStatus = booking.paymentStatus
                    hasPayment = booking.paymentId != null || uploadedImageBase64 != null // Update payment status

                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Booking Details",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            DetailRow(label = "ID", value = "${booking.id}")
                            DetailRow(label = "Date", value = "${booking.date} ${booking.time}")
                            DetailRow(label = "Status", value = booking.status)
                            DetailRow(label = "Service", value = booking.serviceName ?: "Unknown Service")
                            DetailRow(label = "Price", value = "${booking.price ?: "N/A"}")
                            booking.paymentId?.let { DetailRow(label = "Payment ID", value = "$it") }
                            booking.paymentStatus?.let { DetailRow(label = "Payment Status", value = it) }
                            booking.paymentMethod?.let { DetailRow(label = "Payment Method", value = it) }
                        }
                    }

                    // Payment Slip Section
                    (booking.paymentImage ?: uploadedImageBase64)?.let { imageData ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Payment Slip",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = if (imageData.startsWith("data:image")) imageData
                                        else "data:image/jpeg;base64,$imageData" // Ensure base64 is prefixed correctly
                                    ),
                                    contentDescription = "Payment Slip",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }

                    // Payment Actions (only show if no payment exists)
                    if (!hasPayment) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Pay", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }

                        // Preview and Submit
                        imageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Selected Payment Slip",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Image(
                                        painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Selected Payment Slip",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val base64Image = uriToBase64(context, uri)
                                                val result = viewModel.createPayment(
                                                    bookingId = booking.id,
                                                    amount = booking.price ?: 0f,
                                                    paymentMethod = "QR Code",
                                                    image = base64Image
                                                )
                                                result.onSuccess { payment ->
                                                    paymentStatus = "pending"
                                                    uploadedImageBase64 = base64Image // Show uploaded image
                                                    imageUri = null
                                                    hasPayment = true // Hide Pay button
                                                    errorMessage = "Payment submitted successfully!"
                                                    viewModel.fetchBookingDetails() // Refresh server data
                                                }.onFailure { exception ->
                                                    errorMessage = "Payment failed: ${exception.message}"
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("Submit Payment", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    // Error/Success Message
                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = msg,
                            color = if (msg.contains("successfully")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )
                        if (msg.contains("Permission denied")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        Manifest.permission.READ_MEDIA_IMAGES
                                    } else {
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                    }
                                    permissionLauncher.launch(permission)
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Text("Retry")
                            }
                        }
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

// Helper composable for detail rows
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

// Utility function to convert URI to Base64
fun uriToBase64(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bytes = inputStream?.readBytes() ?: byteArrayOf()
    inputStream?.close()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

class BookingDetailViewModel(private val bookingId: Int) : ViewModel() {
    private val apiRepository = ApiRepository.getInstance()
    private val _bookingState = MutableStateFlow<BookingState>(BookingState.Loading)
    val bookingState: StateFlow<BookingState> = _bookingState

    sealed class BookingState {
        object Loading : BookingState()
        data class Success(val booking: Booking) : BookingState()
        data class Error(val message: String) : BookingState()
    }

    init {
        fetchBookingDetails()
    }

    fun fetchBookingDetails() {
        viewModelScope.launch {
            val result = apiRepository.getBookingDetails(bookingId)
            result.onSuccess { booking ->
                _bookingState.value = BookingState.Success(booking)
            }.onFailure { exception ->
                _bookingState.value = BookingState.Error(exception.message ?: "Failed to fetch booking")
            }
        }
    }

    suspend fun createPayment(bookingId: Int, amount: Float, paymentMethod: String, image: String): Result<Payment> {
        return apiRepository.createPayment(bookingId, amount, paymentMethod, image = image)
    }
}

class BookingDetailViewModelFactory(private val bookingId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookingDetailViewModel(bookingId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}