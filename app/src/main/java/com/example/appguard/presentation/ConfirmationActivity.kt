package com.example.appguard.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.appguard.presentation.ui.theme.AppGuardTheme
import com.example.appguard.presentation.ui.ConfirmationOverlay

class ConfirmationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val confirmationCount = intent.getIntExtra("confirmation_count", 10)
        val targetPackage = intent.getStringExtra("target_package") ?: ""
        val targetLabel = intent.getStringExtra("target_label") ?: "this app"

        setContent {
            AppGuardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConfirmationOverlay(
                        confirmationCount = confirmationCount,
                        targetAppLabel = targetLabel,
                        onConfirm = {
                            launchTargetApp(targetPackage)
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun launchTargetApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(launchIntent)
        }
        finish()
    }

    @Deprecated("Use OnBackPressedCallback instead")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}