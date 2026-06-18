package com.example.appguard.presentation.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appguard.presentation.MainViewModel
import com.example.appguard.presentation.ui.AppPickerScreen
import com.example.appguard.presentation.ui.MainScreen

@Composable
fun Navigation(
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        mainViewModel.checkPermissions()
    }

    LaunchedEffect(Unit) {
        mainViewModel.checkPermissions()
    }

    val settings = mainViewModel.settings
    val installedApps = mainViewModel.installedApps

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                settings = settings,
                installedApps = installedApps,
                onSelectApp = { navController.navigate("app_picker") },
                onToggleProtection = mainViewModel::onProtectionToggled,
                onConfirmationCountChanged = mainViewModel::onConfirmationCountChanged,
                onRequestOverlayPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        overlayLauncher.launch(intent)
                    }
                },
                onRequestAccessibilityPermission = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }

        composable("app_picker") {
            AppPickerScreen(
                apps = installedApps,
                onAppSelected = { app ->
                    mainViewModel.onTargetAppSelected(app)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
