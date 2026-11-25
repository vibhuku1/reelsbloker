package com.scrollstop.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.scrollstop.app.ui.screens.AboutScreen
import com.scrollstop.app.ui.screens.HomeScreen
import com.scrollstop.app.ui.screens.MainScaffold
import com.scrollstop.app.ui.screens.OnboardingScreen
import com.scrollstop.app.ui.screens.SettingsScreen
import com.scrollstop.app.ui.screens.StatisticsScreen
import com.scrollstop.app.viewmodel.MainUiState
import com.scrollstop.app.viewmodel.MainViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun ScrollStopNavHost(
    viewModel: MainViewModel,
    uiState: MainUiState,
    hasCompletedOnboarding: Boolean
) {
    val navController: NavHostController = rememberNavController()
    val startDestination = if (hasCompletedOnboarding) Routes.HOME else Routes.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    viewModel.setOnboardingDone(true)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            MainScaffold(
                navController = navController,
                currentRoute = Routes.HOME
            ) {
                HomeScreen(viewModel = viewModel, uiState = uiState)
            }
        }

        composable(Routes.STATISTICS) {
            MainScaffold(
                navController = navController,
                currentRoute = Routes.STATISTICS
            ) {
                StatisticsScreen(viewModel = viewModel, uiState = uiState)
            }
        }

        composable(Routes.SETTINGS) {
            MainScaffold(
                navController = navController,
                currentRoute = Routes.SETTINGS
            ) {
                SettingsScreen(viewModel = viewModel, uiState = uiState)
            }
        }

        composable(Routes.ABOUT) {
            MainScaffold(
                navController = navController,
                currentRoute = Routes.ABOUT
            ) {
                AboutScreen()
            }
        }
    }
}
