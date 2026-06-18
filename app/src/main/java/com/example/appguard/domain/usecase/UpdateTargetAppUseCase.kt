package com.example.appguard.domain.usecase

import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppGuardRepository

class UpdateTargetAppUseCase(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase
) {
    operator fun invoke(packageName: String?, label: String?): AppGuardSettings {
        val current = getSettings()
        val updated = current.copy(
            targetPackageName = packageName,
            targetAppLabel = label,
            isProtectionEnabled = packageName != null
        )
        saveSettings(updated)
        return updated
    }
}