package com.karthik.aegis.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.karthik.aegis.service.LocationTrackingService
import com.karthik.aegis.ui.auth.AuthScreen
import com.karthik.aegis.ui.contacts.ContactsScreen
import com.karthik.aegis.ui.contacts.ContactsViewModel
import com.karthik.aegis.ui.home.HomeScreen
import com.karthik.aegis.ui.map.MapScreen
import com.karthik.aegis.ui.map.MapViewModel
import com.karthik.aegis.ui.sos.SOSScreen
import com.karthik.aegis.ui.sos.SOSViewModel
import com.karthik.aegis.ui.splash.SplashScreen
import com.karthik.aegis.ui.zones.ZoneScreen
import com.karthik.aegis.ui.zones.ZoneViewModel
import com.karthik.aegis.viewmodel.AuthViewModel
import com.karthik.aegis.viewmodel.HomeViewModel

sealed class Route(val route: String) {
    object Splash : Route("splash")
    object Auth : Route("auth")
    object Home : Route("home")
    object Map : Route("map")
    object SOS : Route("sos")
    object Contacts : Route("contacts")
    object Zones : Route("zones")
}

// Bottom navigation tabs
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val iconTint: Color? = null
)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Default.Home, "Home"),
    BottomNavItem("map", Icons.Default.Map, "Map"),
    BottomNavItem("sos", Icons.Default.Warning, "SOS", Color(0xFFFF3D3D)),
    BottomNavItem("contacts", Icons.Default.Person, "Contacts"),
)

@Composable
fun AegisNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    val startDestination = if (auth.currentUser != null) "home" else "auth"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable("splash") {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Authentication Screen
        composable("auth") {
            val authViewModel: AuthViewModel = hiltViewModel()
            val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

            AuthScreen(
                uiState = uiState,
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // Home (with bottom nav)
        composable("home") {
            MainTabScreen(
                navController = navController,
                initialTab = "home"
            )
        }

        // Map (with bottom nav)
        composable("map") {
            MainTabScreen(
                navController = navController,
                initialTab = "map"
            )
        }

        // SOS (with bottom nav)
        composable("sos") {
            MainTabScreen(
                navController = navController,
                initialTab = "sos"
            )
        }

        // Contacts (with bottom nav)
        composable("contacts") {
            MainTabScreen(
                navController = navController,
                initialTab = "contacts"
            )
        }

        // Zones (full screen, no bottom nav)
        composable("zones") {
            val zoneViewModel: ZoneViewModel = hiltViewModel()
            val safeZones by zoneViewModel.safeZones.collectAsStateWithLifecycle()
            val uiState by zoneViewModel.uiState.collectAsStateWithLifecycle()

            ZoneScreen(
                uiState = uiState,
                safeZones = safeZones,
                onAddZone = { zoneViewModel.addZone(it) },
                onRemoveZone = { zoneViewModel.removeZone(it) },
                onUpdateZone = { zoneViewModel.updateZone(it) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainTabScreen(
    navController: NavHostController,
    initialTab: String
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Shared ViewModels across tabs
    val homeViewModel: HomeViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()
    val sosViewModel: SOSViewModel = hiltViewModel()
    val contactsViewModel: ContactsViewModel = hiltViewModel()

    // Collect states
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val familyMembers by homeViewModel.familyMembers.collectAsStateWithLifecycle()
    val familyLocations by homeViewModel.familyLocations.collectAsStateWithLifecycle()
    val activeAlerts by homeViewModel.activeAlerts.collectAsStateWithLifecycle()
    val isTrackingEnabled by homeViewModel.isTrackingEnabled.collectAsStateWithLifecycle()

    val mapFamilyMembers by mapViewModel.familyMembers.collectAsStateWithLifecycle()
    val mapFamilyLocations by mapViewModel.familyLocations.collectAsStateWithLifecycle()
    val safeZones by mapViewModel.safeZones.collectAsStateWithLifecycle()
    val currentUserUid by remember { mutableStateOf(mapViewModel.currentUserUid) }

    val sosUiState by sosViewModel.uiState.collectAsStateWithLifecycle()
    val sosActiveAlerts by sosViewModel.activeAlerts.collectAsStateWithLifecycle()

    val contacts by contactsViewModel.contacts.collectAsStateWithLifecycle()
    val contactsUiState by contactsViewModel.uiState.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(initialTab) }

    // Navigation callback for tab switching
    val onTabSelected: (String) -> Unit = { tab ->
        if (tab != selectedTab) {
            selectedTab = tab
            navController.navigate(tab) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            if (item.label == "SOS" && activeAlerts.isNotEmpty()) {
                                BadgedBox(badge = {
                                    Badge {
                                        Text(
                                            "${activeAlerts.size}",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }) {
                                    Icon(item.icon, item.label, tint = item.iconTint ?: LocalContentColor.current)
                                }
                            } else {
                                Icon(item.icon, item.label, tint = item.iconTint ?: LocalContentColor.current)
                            }
                        },
                        label = { Text(item.label, fontSize = 11.sp) },
                        selected = selectedTab == item.route,
                        onClick = { onTabSelected(item.route) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = if (item.iconTint != null) item.iconTint else MaterialTheme.colorScheme.primary,
                            indicatorColor = if (item.label == "SOS")
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    "home" -> HomeScreen(
                        uiState = homeUiState,
                        familyMembers = familyMembers,
                        familyLocations = familyLocations,
                        activeAlerts = activeAlerts,
                        isTrackingEnabled = isTrackingEnabled,
                        userName = homeViewModel.userName,
                        onNavigateToSOS = { onTabSelected("sos") },
                        onNavigateToMap = { onTabSelected("map") },
                        onNavigateToContacts = { onTabSelected("contacts") },
                        onNavigateToZones = { navController.navigate("zones") },
                        onSignOut = {
                            auth.signOut()
                            navController.navigate("auth") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onToggleTracking = {
                            homeViewModel.toggleLocationTracking()
                            if (!isTrackingEnabled) {
                                LocationTrackingService.startTracking(context, LocationTrackingService.MODE_ACTIVE)
                            } else {
                                LocationTrackingService.stopTracking(context)
                            }
                        }
                    )

                    "map" -> MapScreen(
                        familyMembers = mapFamilyMembers,
                        familyLocations = mapFamilyLocations,
                        safeZones = safeZones,
                        currentUserUid = currentUserUid,
                        onNavigateBack = { onTabSelected("home") }
                    )

                    "sos" -> SOSScreen(
                        uiState = sosUiState,
                        activeAlerts = sosActiveAlerts,
                        onTriggerSOS = { reason -> sosViewModel.triggerSOS(reason) },
                        onResolveSOS = { uid -> sosViewModel.resolveSOSAlert(uid) },
                        onSendSafe = { sosViewModel.sendSafeNotification() },
                        onNavigateBack = { onTabSelected("home") }
                    )

                    "contacts" -> ContactsScreen(
                        contacts = contacts,
                        uiState = contactsUiState,
                        onAddContact = { contactsViewModel.addContact(it) },
                        onRemoveContact = { contactsViewModel.removeContact(it) },
                        onUpdateContact = { contactsViewModel.updateContact(it) },
                        onNavigateBack = { onTabSelected("home") }
                    )
                }
            }
        }
    }
}
