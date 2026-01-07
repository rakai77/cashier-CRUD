package com.example.cashier.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cashier.presentation.screen.camera.CameraScreen
import com.example.cashier.presentation.screen.form.FormScreen
import com.example.cashier.presentation.screen.home.HomeScreen
import com.example.cashier.presentation.screen.home.HomeViewModel
import com.example.cashier.presentation.theme.CashierTheme
import org.koin.androidx.compose.koinViewModel

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
        composable(
            route = "form?mode={mode}&id={id}",
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                    defaultValue = "add"
                },
                navArgument("id") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "add"
            val id = backStackEntry.arguments?.getInt("id")?.takeIf { it != -1 }

            FormScreen(
                navController = navController,
                mode = mode,
                id = id
            )
        }
        composable("camera") {
            CameraScreen(
                onImageCaptured = { uri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("capturedImageUri", uri)
                    navController.popBackStack()
                },
                onError = { exception ->
                    Log.e("CameraScreen", "Error: ${exception.message}")
                    navController.popBackStack()
                }
            )
        }
    }
}
