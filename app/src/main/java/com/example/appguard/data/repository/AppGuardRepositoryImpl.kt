package com.example.appguard.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppGuardRepository
import com.example.appguard.domain.repository.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val SETTINGS_KEY = PreferencesKeys.stringKey("app_guard_settings")
private val JSON = Json { ignoreUnknownKeys = true }

private val Context.dataStore by preferencesDataStore(name = "app_guard_settings")

class AppGuardRepositoryImpl(private val context: Context) : AppGuardRepository {

    private val dataStore get() = context.dataStore

    override suspend fun getSettings(): AppGuardSettings {
        val prefs = dataStore.data.first()
        val jsonString = prefs[SETTINGS_KEY] ?: ""
        return if (jsonString.isNotBlank()) {
            try {
                JSON.decodeFromString(jsonString)
            } catch (e: Exception) {
                AppGuardSettings()
            }
        } else {
            AppGuardSettings()
        }
    }

    override suspend fun saveSettings(settings: AppGuardSettings): Boolean {
        return try {
            val jsonString = JSON.encodeToString(settings)
            dataStore.edit { it[SETTINGS_KEY] = jsonString }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun observeSettings(): Flow<AppGuardSettings> {
        return dataStore.data
            .map { prefs ->
                val jsonString = prefs[SETTINGS_KEY] ?: ""
                if (jsonString.isNotBlank()) {
                    try {
                        JSON.decodeFromString(jsonString)
                    } catch (e: Exception) {
                        AppGuardSettings()
                    }
                } else {
                    AppGuardSettings()
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun getInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launchableApps = pm.queryIntentActivities(launcherIntent, 0)
        
        val launchablePackages = launchableApps.mapNotNull { it.activityInfo?.packageName }.toSet()
        
        return apps
            .filter { it.packageName in launchablePackages }
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || 
                    (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 }
            .map { appInfo ->
                val label = pm.getApplicationLabel(appInfo).toString()
                val icon = appInfo.icon
                AppInfo(appInfo.packageName, label, icon)
            }
            .sortedBy { it.label.lowercase() }
    }
}