package com.karthik.aegis.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.karthik.aegis.ui.auth.AuthScreen
import com.karthik.aegis.ui.contacts.ContactsScreen
import com.karthik.aegis.ui.contacts.ContactsViewModel
import com.karthik.aegis.ui.home.HomeScreen
import com.karthik.aegis.ui.sos.SOSScreen
import com.karthik.aegis.ui.sos.SOSViewModel
import com.karthik.aegis.ui.splash.SplashScreen
import com.karthik.aegis.viewmodel.AuthViewModel
import com.karthik.aegis.viewmodel.HomeViewModel

sealed class Route(val route: String) {
    object Splash : Route("splash")
    object Auth : Route("auth")
    object Home : Route("home")
    object SOS : Route("sos")
    object Contacts : Route("contacts")
}

@Composable
fun AegisNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    
    // Determine start destination based on auth state
    val startDestination = if (auth.currentUser != null) Route.Home.route else Route.Auth.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(Route.Splash.route) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(Route.Auth.route) {
                        popUpTo(Route.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Authentication Screen
        composable(Route.Auth.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

            AuthScreen(
                uiState = uiState,
                onAuthSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Auth.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Home Screen
        composable(Route.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val familyMembers by homeViewModel.familyMembers.collectAsStateWithLifecycle()
            val familyLocations by homeViewModel.familyLocations.collectAsStateWithLifecycle()
            val activeAlerts by homeViewModel.activeAlerts.collectAsStateWithLifecycle()

            HomeScreen(
                uiState = uiState,
                familyMembers = familyMembers,
                familyLocations = familyLocations,
                activeAlerts = activeAlerts,
                onNavigateToSOS = {
                    navController.navigate(Route.SOS.route)
                },
                onNavigateToContacts = {
                    navController.navigate(Route.Contacts.route)
                },
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Route.Auth.route) {
                        popUpTo(Route.Home.route) { inclusive = true }
                    }
                },
                viewModel = homeViewModel,
                context = context
            )
        }

        // SOS Screen
        composable(Route.SOS.route) {
            val sosViewModel: SOSViewModel = hiltViewModel()
            val uiState by sosViewModel.uiState.collectAsStateWithLifecycle()
            val activeAlerts by sosViewModel.activeAlerts.collectAsStateWithLifecycle()

            SOSScreen(
                uiState = uiState,
                activeAlerts = activeAlerts,
                onTriggerSOS = { reason ->
                    sosViewModel.triggerSOS(reason)
                },
                onResolveSOS = { uid ->
                    sosViewModel.resolveSOSAlert(uid)
                },
                onSendSafe = {
                    sosViewModel.sendSafeNotification()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Contacts Screen
        composable(Route.Contacts.route) {
            val contactsViewModel: ContactsViewModel = hiltViewModel()
            val contacts by contactsViewModel.contacts.collectAsStateWithLifecycle()
            val uiState by contactsViewModel.uiState.collectAsStateWithLifecycle()

            ContactsScreen(
                contacts = contacts,
                uiState = uiState,
                onAddContact = { contact ->
                    contactsViewModel.addContact(contact)
                },
                onRemoveContact = { contactId ->
                    contactsViewModel.removeContact(contactId)
                },
                onUpdateContact = { contact ->
                    contactsViewModel.updateContact(contact)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
