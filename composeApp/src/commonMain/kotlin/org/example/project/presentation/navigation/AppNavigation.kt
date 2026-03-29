package org.example.project.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import org.example.project.auth.getPlatformContext
import org.example.project.data.audio.AudioRecorder
import org.example.project.data.repository.DeepgramTranscriptionRepositoryImpl
import org.example.project.data.repository.SupabaseNotesRepositoryImpl
import org.example.project.data.repository.ProfileRepositoryImpl
import org.example.project.data.repository.SubscriptionRepositoryImpl
import org.example.project.domain.repository.IPreferencesRepository
import org.example.project.presentation.screens.HomeScreen
import org.example.project.presentation.screens.LoginScreen
import org.example.project.presentation.screens.NoteDetailsScreen
import org.example.project.presentation.screens.OnboardingScreen
import org.example.project.presentation.screens.PaywallScreen
import org.example.project.presentation.screens.RecordingScreen
import org.example.project.presentation.screens.SettingsScreen
import org.example.project.presentation.viewmodels.HomeViewModel
import org.example.project.presentation.viewmodels.NoteDetailsViewModel
import org.example.project.presentation.viewmodels.OnboardingViewModel
import org.example.project.presentation.viewmodels.PaywallViewModel
import org.example.project.presentation.viewmodels.RecordingViewModel
import org.example.project.presentation.viewmodels.SettingsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val platformContext = getPlatformContext()
    val preferencesRepository = koinInject<IPreferencesRepository>()

    val startDestination: Any = if (preferencesRepository.isOnboardingCompleted()) {
        LoginRoute
    } else {
        OnboardingRoute
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<OnboardingRoute> {
            val viewModel = koinViewModel<OnboardingViewModel>()
            OnboardingScreen(
                viewModel = viewModel,
                onOnboardingFinished = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(HomeRoute) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<HomeRoute> {
            val homeViewModel = koinViewModel<HomeViewModel>()
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToRecording = { navController.navigate(RecordingRoute) },
                onNavigateToNoteDetails = { noteId -> navController.navigate(NoteDetailsRoute(noteId)) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) }
            )
        }
        composable<RecordingRoute> {
            val viewModel = koinViewModel<RecordingViewModel> { parametersOf(platformContext) }

            RecordingScreen(
                viewModel = viewModel,
                onTranscriptionSuccess = { noteId ->
                    navController.navigate(NoteDetailsRoute(noteId)) {
                        popUpTo<HomeRoute>()
                    }
                },
                onNavigateToPaywall = { navController.navigate(PaywallRoute) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<NoteDetailsRoute> { backStackEntry ->
            val route: NoteDetailsRoute = backStackEntry.toRoute()
            val noteDetailsViewModel = koinViewModel<NoteDetailsViewModel> { parametersOf(route.noteId, platformContext) }
            NoteDetailsScreen(
                viewModel = noteDetailsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<SettingsRoute> {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToPaywall = { navController.navigate(PaywallRoute) }
            )
        }
        composable<PaywallRoute> {
            val paywallViewModel = koinViewModel<PaywallViewModel>()
            PaywallScreen(
                viewModel = paywallViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
