package com.example.mobileappcar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mobileappcar.ui.screens.*
import com.example.mobileappcar.ui.theme.MobileappcarTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileappcarTheme {
                AppLayout()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLayout() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isLoggedIn by remember { mutableStateOf(false) } // Track login state

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(navController, drawerState, isLoggedIn)
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route,
                    onProfileClick = {
                        navController.navigate("profile")
                    }
                )
            }
        ) { innerPadding ->
            AppNavigation(navController, Modifier.padding(innerPadding)) { loggedIn ->
                isLoggedIn = loggedIn // Update login state from navigation
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    currentRoute: String?,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Car Spa - ${currentRoute ?: "Login"}") },
        navigationIcon = {
            if (currentRoute != "login" && currentRoute != "register") {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        },
        actions = {
            if (currentRoute != "login" && currentRoute != "register") {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Profile"
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(navController: NavHostController, drawerState: DrawerState, isLoggedIn: Boolean) {
    val scope = rememberCoroutineScope()
    ModalDrawerSheet {
        Text("Menu", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
        Divider()
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = navController.currentDestination?.route == "home",
            onClick = {
                navController.navigate("home")
                scope.launch { drawerState.close() }
            }
        )
        NavigationDrawerItem(
            label = { Text("Bookings") },
            selected = navController.currentDestination?.route == "bookings",
            onClick = {
                navController.navigate("bookings")
                scope.launch { drawerState.close() }
            }
        )
        NavigationDrawerItem(
            label = { Text("Services") },
            selected = navController.currentDestination?.route == "services",
            onClick = {
                navController.navigate("services")
                scope.launch { drawerState.close() }
            }
        )
        NavigationDrawerItem(
            label = { Text("Profile") },
            selected = navController.currentDestination?.route == "profile",
            onClick = {
                navController.navigate("profile")
                scope.launch { drawerState.close() }
            }
        )
        // Show "Register" only if not logged in
        if (!isLoggedIn) {
            NavigationDrawerItem(
                label = { Text("Register") },
                selected = navController.currentDestination?.route == "register",
                onClick = {
                    navController.navigate("register")
                    scope.launch { drawerState.close() }
                }
            )
        }
        Divider()
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                scope.launch { drawerState.close() }
            }
        )
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLoginStateChanged: (Boolean) -> Unit // Callback to update login state
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController, modifier) { isLoggedIn ->
                onLoginStateChanged(isLoggedIn) // Update state on login success
            }
        }
        composable("register") { RegisterScreen(navController, modifier) }
        composable("home") { HomeScreen(navController, modifier) }
        composable("bookings") { BookingListScreen(navController, modifier) }
        composable(
            route = "bookingDetail/{bookingId}",
            arguments = listOf(navArgument("bookingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val bookingId = backStackEntry.arguments?.getInt("bookingId") ?: 0
            BookingDetailScreen(bookingId, navController, modifier)
        }
        composable("services") { ServiceScreen(navController, modifier) }
        composable(
            route = "bookingConfirm/{serviceId}",
            arguments = listOf(navArgument("serviceId") { type = NavType.IntType })
        ) { backStackEntry ->
            val serviceId = backStackEntry.arguments?.getInt("serviceId") ?: 0
            BookingFormScreen(navController = navController, serviceId = serviceId, modifier = modifier)
        }
        composable("profile") { ProfileScreen(navController, modifier) }
    }
}