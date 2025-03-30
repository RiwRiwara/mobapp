package com.example.mobileappcar.ui.navigation

object NavRoutes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Bookings = "bookings"
    const val BookingDetail = "booking_detail/{bookingId}"
    const val Services = "services"
    const val ServiceDetail = "service_detail/{serviceId}"
    const val BookingConfirm = "booking_confirm/{serviceId}/{time}"
    const val Payment = "payment/{bookingId}" // Added Payment route
    const val Profile = "profile"
}