package com.example.appguard.domain.usecase

import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppGuardRepository

class ToggleProtectionUseCase(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase
) {
    suspend operator fun invoke(enabled: Boolean): AppGuardSettings {
        val current = getSettings()
        val updated = current.copy(isProtectionEnabled = enabled)
        saveSettings(updated)
        return updated
    }
}