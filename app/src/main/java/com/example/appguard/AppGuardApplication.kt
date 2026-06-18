package com.example.appguard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appguard.data.repository.AppGuardRepositoryImpl
import com.example.appguard.domain.repository.AppGuardRepository
import com.example.appguard.domain.usecase.GetInstalledAppsUseCase
import com.example.appguard.domain.usecase.GetSettingsUseCase
import com.example.appguard.domain.usecase.SaveSettingsUseCase
import com.example.appguard.domain.usecase.ToggleProtectionUseCase
import com.example.appguard.domain.usecase.UpdateConfirmationCountUseCase
import com.example.appguard.domain.usecase.UpdateTargetAppUseCase
import com.example.appguard.presentation.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class AppGuardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AppGuardApplication)
            modules(appModule)
        }
    }

    companion object {
        private val appModule: Module = module {
            single<AppGuardRepository> { AppGuardRepositoryImpl(androidContext()) }
            single { GetSettingsUseCase(get()) }
            single { SaveSettingsUseCase(get()) }
            single { GetInstalledAppsUseCase(get()) }
            single { ToggleProtectionUseCase(get(), get()) }
            single { UpdateTargetAppUseCase(get(), get()) }
            single { UpdateConfirmationCountUseCase(get(), get()) }

            single<ViewModelProvider.Factory> {
                object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(
                            androidApplication(),
                            get(),
                            get(),
                            get(),
                            get(),
                            get(),
                            get()
                        ) as T
                    }
                }
            }
        }
    }
}
