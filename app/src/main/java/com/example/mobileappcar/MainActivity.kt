package com.example.mobileappcar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mobileappcar.ui.navigation.AppNavigation
import com.example.mobileappcar.ui.navigation.NavRoutes
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
    var isLoggedIn by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(navController, drawerState, isLoggedIn)
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route,
                    onProfileClick = { navController.navigate(NavRoutes.Profile) },
                    isLoggedIn = isLoggedIn
                )
            }
        ) { innerPadding ->
            AppNavigation(navController, Modifier.padding(innerPadding)) { loggedIn ->
                isLoggedIn = loggedIn
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    currentRoute: String?,
    onProfileClick: () -> Unit,
    isLoggedIn: Boolean
) {
    TopAppBar(
        title = {
            when (currentRoute) {
                NavRoutes.Login, NavRoutes.Register -> Text("Car Spa")
                NavRoutes.Home -> Text("Home")
                NavRoutes.Bookings -> Text("My Bookings")
                NavRoutes.Services -> Text("Our Services")
                NavRoutes.Profile -> Text("Profile")
                else -> {
                    if (currentRoute?.startsWith(NavRoutes.ServiceDetail) == true) {
                        Text("Service Details")
                    } else {
                        Text("Car Spa")
                    }
                }
            }
        },
        navigationIcon = {
            if (currentRoute != NavRoutes.Login && currentRoute != NavRoutes.Register) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Open Menu",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = {
            if (isLoggedIn && currentRoute != NavRoutes.Login && currentRoute != NavRoutes.Register) {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = { /* Handle notifications */ }) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors( // Changed to topAppBarColors
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    isLoggedIn: Boolean
) {
    val scope = rememberCoroutineScope()

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Header
        Text(
            text = "Car Spa Menu",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        // Main Navigation Items
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = navController.currentDestination?.route == NavRoutes.Home,
            onClick = {
                navController.navigate(NavRoutes.Home) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                }
                scope.launch { drawerState.close() }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.AccountBox, contentDescription = "Bookings") },
            label = { Text("My Bookings") },
            selected = navController.currentDestination?.route == NavRoutes.Bookings,
            onClick = {
                navController.navigate(NavRoutes.Bookings)
                scope.launch { drawerState.close() }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Build, contentDescription = "Services") },
            label = { Text("Our Services") },
            selected = navController.currentDestination?.route == NavRoutes.Services,
            onClick = {
                navController.navigate(NavRoutes.Services)
                scope.launch { drawerState.close() }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Conditional Items (Login/Register)
        if (!isLoggedIn) {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = "Register") },
                label = { Text("Register") },
                selected = navController.currentDestination?.route == NavRoutes.Register,
                onClick = {
                    navController.navigate(NavRoutes.Register)
                    scope.launch { drawerState.close() }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Logout or Login
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.ExitToApp, contentDescription = if (isLoggedIn) "Logout" else "Login") },
            label = { Text(if (isLoggedIn) "Logout" else "Login") },
            selected = false,
            onClick = {
                if (isLoggedIn) {
                    navController.navigate(NavRoutes.Login) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                } else {
                    navController.navigate(NavRoutes.Login)
                }
                scope.launch { drawerState.close() }
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}