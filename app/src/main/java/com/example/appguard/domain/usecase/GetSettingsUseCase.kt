package com.example.appguard.domain.usecase

import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppGuardRepository

class GetSettingsUseCase(private val repository: AppGuardRepository) {
    suspend operator fun invoke(): AppGuardSettings = repository.getSettings()
    
    fun observe(): kotlinx.coroutines.flow.Flow<AppGuardSettings> = repository.observeSettings()
}