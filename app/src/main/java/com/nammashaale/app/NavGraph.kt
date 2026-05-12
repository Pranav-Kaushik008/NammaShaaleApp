package com.nammashaale.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.nammashaale.app.ui.screens.*

@Composable
fun NammaShaaleNavGraph(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val startDest = if (auth.currentUser != null) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDest) {
        // Login screen with optional registered flag
        composable(
            route = "login?registered={registered}",
            arguments = listOf(
                navArgument("registered") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStack ->
            val justRegistered = backStack.arguments?.getBoolean("registered") ?: false
            LoginScreen(navController, justRegistered = justRegistered)
        }

        // Register screen
        composable("register") {
            RegisterScreen(navController)
        }

        composable("dashboard")    { DashboardScreen(navController) }
        composable("asset_list")   { AssetListScreen(navController) }
        composable("add_asset")    { AddAssetScreen(navController) }
        composable("health_check") { HealthCheckScreen(navController) }
        composable("repairs")      { RepairListScreen(navController) }
        composable("reports")      { ReportScreen(navController) }
        composable("asset_detail/{assetId}") { backStack ->
            val id = backStack.arguments?.getString("assetId")?.toIntOrNull() ?: 0
            AssetDetailScreen(navController, id)
        }
    }
}
