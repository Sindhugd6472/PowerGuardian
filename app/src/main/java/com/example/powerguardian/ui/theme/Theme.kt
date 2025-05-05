package com.example.powerguardian.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PowerGuardianColorScheme = darkColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = Color.White
)

@Composable
fun PowerGuardianTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PowerGuardianColorScheme,
        typography = Typography(),
        content = content
    )
}
