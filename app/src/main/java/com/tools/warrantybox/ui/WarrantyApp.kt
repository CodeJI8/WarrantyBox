package com.tools.warrantybox.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tools.warrantybox.ui.screens.AddEditScreen
import com.tools.warrantybox.ui.screens.DetailScreen
import com.tools.warrantybox.ui.screens.ListScreen

@Composable
fun WarrantyApp() {
    val navController = rememberNavController()
    val viewModel: WarrantyViewModel = viewModel()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ListScreen(
                viewModel = viewModel,
                onNavigateToAdd = { navController.navigate("add_edit/-1") },
                onNavigateToDetail = { id -> navController.navigate("detail/$id") }
            )
        }
        composable(
            route = "add_edit/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: -1L
            AddEditScreen(
                viewModel = viewModel,
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "detail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: -1L
            DetailScreen(
                viewModel = viewModel,
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate("add_edit/$itemId") }
            )
        }
    }
}
