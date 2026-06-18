package com.example.appguard.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.example.appguard.AppGuardApplication
import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.usecase.GetSettingsUseCase
import org.koin.core.koin

class AppGuardAccessibilityService : AccessibilityService() {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private var settings: AppGuardSettings = AppGuardSettings()
    private var lastTargetPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        loadSettings()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
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
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Additional check for content changes
            }
        }
    }

    override fun onInterrupt() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    private fun loadSettings() {
        try {
            val getSettings = koin.get<GetSettingsUseCase>()
            settings = getSettings()
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