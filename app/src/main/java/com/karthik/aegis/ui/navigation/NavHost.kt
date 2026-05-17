package com.karthik.aegis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.karthik.aegis.ui.auth.AuthScreen
import com.karthik.aegis.ui.auth.AuthViewModel
import com.karthik.aegis.ui.contacts.ContactsScreen
import com.karthik.aegis.ui.contacts.ContactsViewModel
import com.karthik.aegis.ui.home.HomeScreen
import com.karthik.aegis.ui.sos.SOSScreen
import com.karthik.aegis.ui.sos.SOSViewModel
import com.karthik.aegis.ui.home.HomeViewModel
import com.karthik.aegis.ui.splash.SplashScreen
import com.karthik.aegis.ui.splash.SplashViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Contacts : Screen("contacts")
    object Zones : Screen("zones")
    object SOS : Screen("sos")
    object Settings : Screen("settings")
    object Family : Screen("family")
}

@Composable
fun AegisNavHost(navController: NavHostController) {
    val splashViewModel: SplashViewModel = hiltViewModel()
    val isLoggedIn by splashViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSOS = { navController.navigate(Screen.SOS.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToZones = { navController.navigate(Screen.Zones.route) },
                onNavigateToFamily = { navController.navigate(Screen.Family.route) }
            )
        }

        composable(Screen.SOS.route) {
            val viewModel: SOSViewModel = hiltViewModel()
            SOSScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Contacts.route) {
            val viewModel: ContactsViewModel = hiltViewModel()
            ContactsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Zones.route) {
            // Zones Screen
        }

        composable(Screen.Family.route) {
            // Family Screen
        }
    }
}