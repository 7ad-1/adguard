package com.example.appguard.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppGuardSettings(
    val targetPackageName: String? = null,
    val targetAppLabel: String? = null,
    val confirmationCount: Int = 10,
    val isProtectionEnabled: Boolean = false,
    val isOverlayPermissionGranted: Boolean = false,
    val isAccessibilityEnabled: Boolean = false
)

sealed interface AppGuardEvent {
    data class TargetAppLaunched(val packageName: String) : AppGuardEvent
    data class ConfirmationCompleted(val remaining: Int) : AppGuardEvent
    data class ProtectionToggled(val enabled: Boolean) : AppGuardEvent
    data class SettingsUpdated(val settings: AppGuardSettings) : AppGuardEvent
}