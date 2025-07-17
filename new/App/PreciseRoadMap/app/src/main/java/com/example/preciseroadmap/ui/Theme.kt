package com.example.preciseroadmap.ui

import android.content.Context
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import com.example.preciseroadmap.data.local.SettingsStore

@Composable
fun DynamicTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val store = remember { SettingsStore(context) }
    val themeSetting by store.theme.collectAsState(initial = "system")

    val isDark = when (themeSetting) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (isDark) darkColorScheme() else lightColorScheme(),
        content = content
    )
}