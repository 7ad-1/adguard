package com.example.appguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.runBlocking
import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.usecase.GetSettingsUseCase
import com.example.appguard.presentation.ConfirmationActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppGuardAccessibilityService : AccessibilityService(), KoinComponent {

    private var settings: AppGuardSettings = AppGuardSettings()
    private var lastTargetPackage: String? = null
    private val getSettingsUseCase: GetSettingsUseCase by inject()

    override fun onServiceConnected() {
        super.onServiceConnected()
        loadSettings()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!settings.isProtectionEnabled || settings.targetPackageName == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString()
                if (packageName == settings.targetPackageName && packageName != lastTargetPackage) {
                    lastTargetPackage = packageName
                    showConfirmationScreen()
                }
            }
        }
    }

    override fun onInterrupt() {}

    private fun loadSettings() {
        try {
            settings = runBlocking { getSettingsUseCase() }
        } catch (e: Exception) {
            Log.e("AppGuard", "Failed to load settings", e)
        }
    }

    private fun showConfirmationScreen() {
        val intent = Intent(this, ConfirmationActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )
            putExtra("confirmation_count", settings.confirmationCount)
            putExtra("target_package", settings.targetPackageName)
            putExtra("target_label", settings.targetAppLabel ?: "this app")
        }
        startActivity(intent)
    }
}
