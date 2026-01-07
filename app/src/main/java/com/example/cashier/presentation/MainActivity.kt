package com.example.cashier.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cashier.presentation.screen.camera.CameraScreen
import com.example.cashier.presentation.screen.home.HomeScreen
import com.example.cashier.presentation.theme.CashierTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashierTheme {
                CashierAppNavHost()
            }
        }
    }
}

@Composable
fun CashierAppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("camera") {
            CameraScreen(
                onImageCaptured = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("capturedImageUri", it)
                    navController.popBackStack()
                },
                onError = { 
                    navController.popBackStack()
                }
            )
        }
    }
}