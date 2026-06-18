package com.example.appguard.domain.usecase

import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppGuardRepository

class SaveSettingsUseCase(private val repository: AppGuardRepository) {
    operator fun invoke(settings: AppGuardSettings): Boolean = repository.saveSettings(settings)
}