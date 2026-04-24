package com.karthik.aegis.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SOSAlert
import com.karthik.aegis.model.TrackedLocation
import com.karthik.aegis.repository.FamilyRepository
import com.karthik.aegis.repository.LocationRepository
import com.karthik.aegis.service.LocationTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val locationRepository = LocationRepository()
    private val familyRepository = FamilyRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val familyMembers: StateFlow<List<FamilyMember>> = flow {
        // Observe family members would be implemented here
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyLocations: StateFlow<Map<String, TrackedLocation>> = flow {
        // Observe locations would be implemented here
        emit(emptyMap())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val activeAlerts: StateFlow<List<SOSAlert>> = flow {
        // Observe alerts would be implemented here
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startLocationTracking() {
        LocationTrackingService.startTracking(android.app.Application())
    }

    fun stopLocationTracking() {
        LocationTrackingService.stopTracking(android.app.Application())
    }
}

data class HomeUiState(
    val currentUserName: String = "",
    val isTrackingEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)