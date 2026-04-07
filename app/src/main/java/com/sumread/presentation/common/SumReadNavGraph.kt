package com.sumread.presentation.common

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sumread.presentation.settings.SettingsScreen
import com.sumread.presentation.settings.SettingsViewModel

private const val SETTINGS_ROUTE = "settings"

@Composable
fun SumReadNavGraph(
    viewModel: SettingsViewModel,
    onOpenOverlaySettings: () -> Unit,
    onRequestMicrophonePermission: () -> Unit,
    onOpenTtsSettings: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = SETTINGS_ROUTE,
    ) {
        composable(route = SETTINGS_ROUTE) {
            SettingsScreen(
                viewModel = viewModel,
                onOpenOverlaySettings = onOpenOverlaySettings,
                onRequestMicrophonePermission = onRequestMicrophonePermission,
                onOpenTtsSettings = onOpenTtsSettings,
            )
        }
    }
}
