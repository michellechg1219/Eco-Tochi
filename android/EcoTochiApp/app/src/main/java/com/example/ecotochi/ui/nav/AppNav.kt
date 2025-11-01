package com.example.ecotochi.ui.nav

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecotochi.ui.history.HistoryScreen
import com.example.ecotochi.ui.home.HomeScreen
import com.example.ecotochi.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val HISTORY = "history"
}

@Composable
fun App() {
    val nav = rememberNavController()
    MaterialTheme {
        NavHost(navController = nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) {
                HomeScreen(
                    onGoSettings = { nav.navigate(Routes.SETTINGS) },
                    onGoHistory  = { nav.navigate(Routes.HISTORY) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { nav.navigateUp() })
            }
            composable(Routes.HISTORY) {
                HistoryScreen(onBack = { nav.navigateUp() })
            }
        }
    }
}
