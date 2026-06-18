package com.example.appguard.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppGuardRepository
import com.example.appguard.domain.repository.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged

private val KEY_TARGET_PACKAGE = stringPreferencesKey("target_package")
private val KEY_TARGET_LABEL = stringPreferencesKey("target_label")
private val KEY_CONFIRMATION_COUNT = intPreferencesKey("confirmation_count")
private val KEY_PROTECTION_ENABLED = booleanPreferencesKey("protection_enabled")
private val KEY_OVERLAY_PERMISSION = booleanPreferencesKey("overlay_permission")
private val KEY_ACCESSIBILITY_ENABLED = booleanPreferencesKey("accessibility_enabled")

private val Context.dataStore by preferencesDataStore(name = "app_guard_settings")

class AppGuardRepositoryImpl(private val context: Context) : AppGuardRepository {

    private val dataStore get() = context.dataStore

    override suspend fun getSettings(): AppGuardSettings {
        val prefs = dataStore.data.first()
        return AppGuardSettings(
            targetPackageName = prefs[KEY_TARGET_PACKAGE],
            targetAppLabel = prefs[KEY_TARGET_LABEL],
            confirmationCount = prefs[KEY_CONFIRMATION_COUNT] ?: 10,
            isProtectionEnabled = prefs[KEY_PROTECTION_ENABLED] ?: false,
            isOverlayPermissionGranted = prefs[KEY_OVERLAY_PERMISSION] ?: false,
            isAccessibilityEnabled = prefs[KEY_ACCESSIBILITY_ENABLED] ?: false
        )
    }

    override suspend fun saveSettings(settings: AppGuardSettings): Boolean {
        return try {
            dataStore.edit { prefs ->
                settings.targetPackageName?.let { prefs[KEY_TARGET_PACKAGE] = it }
                    ?: prefs.remove(KEY_TARGET_PACKAGE)
                settings.targetAppLabel?.let { prefs[KEY_TARGET_LABEL] = it }
                    ?: prefs.remove(KEY_TARGET_LABEL)
                prefs[KEY_CONFIRMATION_COUNT] = settings.confirmationCount
                prefs[KEY_PROTECTION_ENABLED] = settings.isProtectionEnabled
                prefs[KEY_OVERLAY_PERMISSION] = settings.isOverlayPermissionGranted
                prefs[KEY_ACCESSIBILITY_ENABLED] = settings.isAccessibilityEnabled
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun observeSettings(): Flow<AppGuardSettings> {
        return dataStore.data
            .map { prefs ->
                AppGuardSettings(
                    targetPackageName = prefs[KEY_TARGET_PACKAGE],
                    targetAppLabel = prefs[KEY_TARGET_LABEL],
                    confirmationCount = prefs[KEY_CONFIRMATION_COUNT] ?: 10,
                    isProtectionEnabled = prefs[KEY_PROTECTION_ENABLED] ?: false,
                    isOverlayPermissionGranted = prefs[KEY_OVERLAY_PERMISSION] ?: false,
                    isAccessibilityEnabled = prefs[KEY_ACCESSIBILITY_ENABLED] ?: false
                )
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
