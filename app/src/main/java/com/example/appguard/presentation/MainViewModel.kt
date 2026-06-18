package com.example.appguard.presentation

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppInfo
import com.example.appguard.domain.usecase.GetInstalledAppsUseCase
import com.example.appguard.domain.usecase.GetSettingsUseCase
import com.example.appguard.domain.usecase.SaveSettingsUseCase
import com.example.appguard.domain.usecase.ToggleProtectionUseCase
import com.example.appguard.domain.usecase.UpdateConfirmationCountUseCase
import com.example.appguard.domain.usecase.UpdateTargetAppUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(
    application: Application,
    private val getSettings: GetSettingsUseCase,
    private val saveSettings: SaveSettingsUseCase,
    private val getInstalledApps: GetInstalledAppsUseCase,
    private val toggleProtection: ToggleProtectionUseCase,
    private val updateTargetApp: UpdateTargetAppUseCase,
    private val updateConfirmationCount: UpdateConfirmationCountUseCase
) : AndroidViewModel(application) {

    var settings by mutableStateOf(AppGuardSettings())
        private set

    var installedApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadData()
        observeSettings()
    }

    private fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                installedApps = getInstalledApps()
                settings = getSettings()
            } catch (e: Exception) {
                errorMessage = "Failed to load data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            getSettings.observe().collect { newSettings ->
                settings = newSettings
            }
        }
    }

    fun onTargetAppSelected(app: AppInfo?) {
        viewModelScope.launch {
            val updated = updateTargetApp(app?.packageName, app?.label)
            settings = updated
        }
    }

    fun onProtectionToggled(enabled: Boolean) {
        viewModelScope.launch {
            val updated = toggleProtection(enabled)
            settings = updated
            if (enabled) {
                checkPermissions()
            }
        }
    }

    fun onConfirmationCountChanged(count: Int) {
        viewModelScope.launch {
            val updated = updateConfirmationCount(count)
            settings = updated
        }
    }

    fun checkPermissions() {
        val app = getApplication<Application>()
        val overlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(app)
        } else {
            true
        }

        viewModelScope.launch {
            val updated = settings.copy(isOverlayPermissionGranted = overlayGranted)
            saveSettings(updated)
            settings = updated
        }
    }
}