package com.example.appguard.domain.repository

import com.example.appguard.domain.model.AppGuardSettings
import kotlinx.coroutines.flow.Flow

interface AppGuardRepository {
    suspend fun getSettings(): AppGuardSettings
    suspend fun saveSettings(settings: AppGuardSettings): Boolean
    fun observeSettings(): Flow<AppGuardSettings>
    
    suspend fun getInstalledApps(): List<AppInfo>
}

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Int
)