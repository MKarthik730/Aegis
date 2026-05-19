package com.karthik.aegis.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.karthik.aegis.model.FamilyMember
import com.karthik.aegis.model.SOSAlert
import com.karthik.aegis.model.TrackedLocation
import com.karthik.aegis.repository.FamilyRepository
import com.karthik.aegis.repository.LocationRepository
import com.karthik.aegis.repository.SOSRepository
import com.karthik.aegis.service.LocationTrackingService
import com.karthik.aegis.utils.AegisPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val familyRepository: FamilyRepository,
    private val sosRepository: SOSRepository,
    private val prefs: AegisPrefs,
    @ApplicationContext private val context: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val groupId = prefs.getFamilyGroupId() ?: "global"

    val familyMembers: StateFlow<List<FamilyMember>> = familyRepository
        .observeFamilyMembers(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyLocations: StateFlow<Map<String, TrackedLocation>> = locationRepository
        .observeFamilyLocations(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val activeAlerts: StateFlow<List<SOSAlert>> = sosRepository
        .observeSOSAlerts(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startLocationTracking() {
        LocationTrackingService.startTracking(context)
        prefs.setLocationTrackingEnabled(true)
    }

    fun stopLocationTracking() {
        LocationTrackingService.stopTracking(context)
        prefs.setLocationTrackingEnabled(false)
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)
