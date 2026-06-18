package com.example.appguard.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appguard.presentation.MainViewModel
import com.example.appguard.presentation.ui.AppPickerScreen
import com.example.appguard.presentation.ui.MainScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit,
    viewModel: MainViewModel
) {
    val settings = viewModel.settings
    val installedApps = viewModel.installedApps

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                settings = settings,
                installedApps = installedApps,
                onSelectApp = { navController.navigate("app_picker") },
                onToggleProtection = viewModel::onProtectionToggled,
                onConfirmationCountChanged = viewModel::onConfirmationCountChanged,
                onRequestOverlayPermission = onRequestOverlayPermission,
                onRequestAccessibilityPermission = onRequestAccessibilityPermission
            )
        }

        composable("app_picker") {
            AppPickerScreen(
                apps = installedApps,
                onAppSelected = { app ->
                    viewModel.onTargetAppSelected(app)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
