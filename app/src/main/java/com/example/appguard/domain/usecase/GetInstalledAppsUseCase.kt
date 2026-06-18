package com.example.appguard.domain.usecase

import com.example.appguard.domain.repository.AppGuardRepository
import com.example.appguard.domain.repository.AppInfo

class GetInstalledAppsUseCase(private val repository: AppGuardRepository) {
    suspend operator fun invoke(): List<AppInfo> = repository.getInstalledApps()
}