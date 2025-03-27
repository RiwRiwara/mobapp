//com/example/mobileappcar/ui/screens/HomeScreen.kt
package com.example.mobileappcar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Car Spa")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("services") }) {
            Text("Book a Service")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("bookings") }) {
            Text("View Bookings")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.navigate("profile") }) {
            Text("Profile")
        }
    }
}