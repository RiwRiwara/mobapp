```
com.example.mobileappcar/
├── data/
│   ├── local/              # Mock data and local storage
│   │   └── MockData.kt
│   ├── remote/             # Future API-related classes
│   │   └── ApiService.kt   # Placeholder
│   └── repository/         # Data handling logic
│       └── BookingRepository.kt
├── model/                  # Data models
│   ├── Booking.kt
│   ├── User.kt
│   ├── Service.kt
│   └── Payment.kt
├── ui/
│   ├── screens/            # Main composable screens
│   │   ├── HomeScreen.kt
│   │   ├── BookingListScreen.kt
│   │   ├── BookingDetailScreen.kt
│   │   ├── ServiceScreen.kt
│   │   ├── ProfileScreen.kt
│   │   └── LoginScreen.kt
│   ├── components/         # Reusable UI components
│   │   └── BookingItem.kt
│   └── theme/              # Theme-related files
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── util/                   # Utility functions (optional)
│   └── DateUtils.kt        # For future date formatting
└── MainActivity.kt         # Entry point
```