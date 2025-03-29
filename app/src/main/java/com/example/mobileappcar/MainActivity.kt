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
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route,
                    onProfileClick = { navController.navigate(NavRoutes.Profile) },
                    isLoggedIn = isLoggedIn // Pass isLoggedIn
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
    isLoggedIn: Boolean // Add this parameter
) {
    TopAppBar(
        title = { Text("Car Spa - ${currentRoute ?: "Login"}") },
        navigationIcon = {
            if (currentRoute != NavRoutes.Login && currentRoute != NavRoutes.Register) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu")
                }
            }
        },
        actions = {
            if (isLoggedIn && currentRoute != NavRoutes.Login && currentRoute != NavRoutes.Register) { // Check isLoggedIn
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
fun NavigationDrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    isLoggedIn: Boolean
) {
    val scope = rememberCoroutineScope()
    ModalDrawerSheet {
        Text(
            text = "Menu",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.headlineSmall
        )
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = navController.currentDestination?.route == NavRoutes.Home,
            onClick = {
                navController.navigate(NavRoutes.Home)
                scope.launch { drawerState.close() }
            }
        )
        NavigationDrawerItem(
            label = { Text("Bookings") },
            selected = navController.currentDestination?.route == NavRoutes.Bookings,
            onClick = {
                navController.navigate(NavRoutes.Bookings)
                scope.launch { drawerState.close() }
            }
        )
        NavigationDrawerItem(
            label = { Text("Services") },
            selected = navController.currentDestination?.route == NavRoutes.Services,
            onClick = {
                navController.navigate(NavRoutes.Services)
                scope.launch { drawerState.close() }
            }
        )
        if (!isLoggedIn) {
            NavigationDrawerItem(
                label = { Text("Register") },
                selected = navController.currentDestination?.route == NavRoutes.Register,
                onClick = {
                    navController.navigate(NavRoutes.Register)
                    scope.launch { drawerState.close() }
                }
            )
        }
        Divider()
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = {
                navController.navigate(NavRoutes.Login) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
                scope.launch { drawerState.close() }
            }
        )
    }
}