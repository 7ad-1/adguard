package com.example.appguard.presentation.ui

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.appguard.domain.model.AppGuardSettings
import com.example.appguard.domain.repository.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settings: AppGuardSettings,
    installedApps: List<AppInfo>,
    onSelectApp: () -> Unit,
    onToggleProtection: (Boolean) -> Unit,
    onConfirmationCountChanged: (Int) -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AppGuard",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status card
            StatusCard(settings = settings)

            // Target app section
            SectionCard(title = "Target App") {
                TargetAppSection(
                    settings = settings,
                    onSelectApp = onSelectApp
                )
            }

            // Confirmation count section
            SectionCard(title = "Confirmations Required") {
                ConfirmationCountSection(
                    count = settings.confirmationCount,
                    onCountChanged = onConfirmationCountChanged
                )
            }

            // Protection toggle
            SectionCard(title = "Protection") {
                ProtectionToggleSection(
                    enabled = settings.isProtectionEnabled,
                    onToggle = onToggleProtection
                )
            }

            // Permissions section
            SectionCard(title = "Permissions") {
                PermissionsSection(
                    isOverlayGranted = settings.isOverlayPermissionGranted,
                    isAccessibilityEnabled = settings.isAccessibilityEnabled,
                    onRequestOverlay = onRequestOverlayPermission,
                    onRequestAccessibility = onRequestAccessibilityPermission
                )
            }

            // Instructions
            SectionCard(title = "How It Works") {
                InstructionsSection()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatusCard(settings: AppGuardSettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (settings.isProtectionEnabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (settings.isProtectionEnabled) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (settings.isProtectionEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (settings.isProtectionEnabled) "Protection Active" else "Protection Inactive",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = if (settings.targetPackageName != null) {
                        "Protecting: ${settings.targetAppLabel ?: settings.targetPackageName}"
                    } else {
                        "No app selected"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun TargetAppSection(
    settings: AppGuardSettings,
    onSelectApp: () -> Unit
) {
    val context = LocalContext.current
    val appIcon = settings.targetPackageName?.let { pkg ->
        try {
            context.packageManager.getApplicationIcon(pkg)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelectApp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (appIcon != null) {
            Image(
                bitmap = appIcon.toBitmap().asImageBitmap(),
                contentDescription = settings.targetAppLabel,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = settings.targetAppLabel ?: "No app selected",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            if (settings.targetPackageName == null) {
                Text(
                    text = "Tap to select an app to protect",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = settings.targetPackageName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Select",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ConfirmationCountSection(
    count: Int,
    onCountChanged: (Int) -> Unit
) {
    var sliderValue by remember { mutableFloatStateOf(count.toFloat()) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Required taps before opening",
                fontSize = 14.sp
            )
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onCountChanged(sliderValue.toInt()) },
            valueRange = 1f..50f,
            steps = 48,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("50", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ProtectionToggleSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Enable Protection",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = if (enabled) "Protection is active" else "Protection is disabled",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun PermissionsSection(
    isOverlayGranted: Boolean,
    isAccessibilityEnabled: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestAccessibility: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Overlay permission
        PermissionItem(
            title = "Overlay Permission",
            description = "Required to show confirmation screen",
            isGranted = isOverlayGranted,
            onRequest = onRequestOverlay
        )

        // Accessibility permission
        PermissionItem(
            title = "Accessibility Service",
            description = "Required to detect app launches",
            isGranted = isAccessibilityEnabled,
            onRequest = onRequestAccessibility
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGranted) Icons.Filled.Check else Icons.Filled.Warning,
            contentDescription = null,
            tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!isGranted) {
            Button(
                onClick = onRequest,
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Grant", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun InstructionsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        InstructionItem(
            number = "1",
            text = "Select an app you want to protect"
        )
        InstructionItem(
            number = "2",
            text = "Set how many confirmations are required"
        )
        InstructionItem(
            number = "3",
            text = "Enable protection and grant permissions"
        )
        InstructionItem(
            number = "4",
            text = "When you try to open the protected app, you'll need to confirm multiple times"
        )
    }
}

@Composable
private fun InstructionItem(
    number: String,
    text: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}