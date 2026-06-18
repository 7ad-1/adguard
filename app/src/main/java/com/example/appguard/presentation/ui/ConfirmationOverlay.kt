package com.example.appguard.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appguard.presentation.ui.theme.BlockerBg
import com.example.appguard.presentation.ui.theme.BlockerCardBg
import com.example.appguard.presentation.ui.theme.WarningOrange
import com.example.appguard.presentation.ui.theme.WarningRed

@Composable
fun ConfirmationOverlay(
    confirmationCount: Int,
    targetAppLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var currentTap by remember { mutableIntStateOf(0) }
    val progress by animateFloatAsState(
        targetValue = currentTap.toFloat() / confirmationCount,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            currentTap >= confirmationCount -> Color(0xFF4CAF50)
            currentTap >= confirmationCount * 0.7 -> WarningOrange
            else -> WarningRed
        },
        label = "progressColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlockerBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Warning",
                modifier = Modifier.size(80.dp),
                tint = WarningRed
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Access Blocked",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You're trying to open",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                text = targetAppLabel,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarningOrange,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = Color.Gray.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap $currentTap / $confirmationCount",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (currentTap < confirmationCount) {
                    "Tap the button ${confirmationCount - currentTap} more time${if (confirmationCount - currentTap != 1) "s" else ""} to open"
                } else {
                    "Ready to open!"
                },
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Confirm button
            Button(
                onClick = {
                    currentTap++
                    if (currentTap >= confirmationCount) {
                        onConfirm()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (currentTap >= confirmationCount) {
                        Color(0xFF4CAF50)
                    } else {
                        WarningRed
                    }
                )
            ) {
                Text(
                    text = if (currentTap >= confirmationCount) {
                        "Open $targetAppLabel"
                    } else {
                        "Tap to confirm (${currentTap + 1}/${confirmationCount})"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cancel button
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}