package com.example.mobileappcar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mobileappcar.ui.screens.booking.BookingDetailScreen
import com.example.mobileappcar.ui.screens.booking.BookingFormScreen
import com.example.mobileappcar.ui.screens.booking.BookingListScreen
import com.example.mobileappcar.ui.screens.home.HomeScreen
import com.example.mobileappcar.ui.screens.login.LoginScreen
import com.example.mobileappcar.ui.screens.profile.ProfileScreen
import com.example.mobileappcar.ui.screens.register.RegisterScreen
import com.example.mobileappcar.ui.screens.service.ServiceScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLoginStateChanged: (Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = NavRoutes.Login) {
        composable(NavRoutes.Login) {
            LoginScreen(navController, modifier) { isLoggedIn ->
                onLoginStateChanged(isLoggedIn)
            }
        }
        composable(NavRoutes.Register) { RegisterScreen(navController, modifier) }
        composable(NavRoutes.Home) { HomeScreen(navController, modifier) }
        composable(NavRoutes.Bookings) { BookingListScreen(navController, modifier) }
        composable(
            route = NavRoutes.BookingDetail, // Uses "bookingDetail/{bookingId}"
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
            BookingDetailScreen(bookingId, navController, modifier)
        }
        composable(NavRoutes.Services) { ServiceScreen(navController, modifier) }
        composable(
            route = NavRoutes.BookingConfirm,
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: 0
            BookingFormScreen(navController, serviceId, modifier)
        }
        composable(NavRoutes.Profile) { ProfileScreen(navController, modifier) }
    }
}