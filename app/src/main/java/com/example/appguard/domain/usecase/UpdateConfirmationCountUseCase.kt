package com.example.appguard.domain.usecase

import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppGuardRepository

class UpdateConfirmationCountUseCase(
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase
) {
    suspend operator fun invoke(count: Int): AppGuardSettings {
        val current = getSettings()
        val clampedCount = count.coerceIn(1, 50)
        val updated = current.copy(confirmationCount = clampedCount)
        saveSettings(updated)
        return updated
    }
}